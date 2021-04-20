package de.havemann.lukas.vanillahttp.dispatcher;

import de.havemann.lukas.vanillahttp.protocol.request.HttpRequest;
import de.havemann.lukas.vanillahttp.protocol.request.HttpRequestBuffer;
import de.havemann.lukas.vanillahttp.protocol.request.HttpRequestParsingException;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponse;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponseWriter;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UnlimitedThreadDispatcher implements ClientSocketDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(UnlimitedThreadDispatcher.class);
    public static final HttpProtocol DEFAULT_HTTP_PROTOCOL = HttpProtocol.HTTP_1_1;

    private final AtomicInteger id = new AtomicInteger(1);
    private final BeanFactory beanFactory;

    @DurationUnit(ChronoUnit.MILLIS)
    @Value("${vanilla.server.http.keepAliveTimeout}")
    private Duration keepAliveTimeout;

    @DataSizeUnit(DataUnit.BYTES)
    @Value("${vanilla.server.http.chunkedEncodingBufferSize}")
    private DataSize chunkedEncodingBufferSize;

    public UnlimitedThreadDispatcher(@Autowired BeanFactory beanFactory) {
        this.beanFactory = Objects.requireNonNull(beanFactory);
    }

    public void dispatch(Socket clientSocket) {
        final ClientConnectionHandlerThread clientConnectionHandlerThread = new ClientConnectionHandlerThread(clientSocket);
        clientConnectionHandlerThread.setDaemon(true);
        clientConnectionHandlerThread.start();
    }

    public class ClientConnectionHandlerThread extends Thread {

        private final Socket clientSocket;
        private ClientRequestProcessor clientRequestProcessor;
        private HttpResponseWriter responseWriter;
        private HttpRequestBuffer requestBuffer;

        public ClientConnectionHandlerThread(Socket clientSocket) {
            setName("client-" + id.incrementAndGet());
            this.clientSocket = Objects.requireNonNull(clientSocket);
        }

        public void run() {
            if (!setupReaderAndWriter()) {
                return;
            }

            Optional<HttpRequest> httpRequest = Optional.empty();
            try {
                while (!clientSocket.isClosed()) {
                    // use http protocol form previous request as default
                    final HttpProtocol protocol = httpRequest.map(HttpRequest::getHttpProtocol).orElse(DEFAULT_HTTP_PROTOCOL);
                    final HttpResponse.Builder response = new HttpResponse.Builder(protocol);

                    try {
                        httpRequest = requestBuffer.readRequest();
                    } catch (HttpRequestParsingException ex) {
                        LOG.error("parsing error", ex);
                        responseWriter.write(response.statusCode(HttpStatusCode.BAD_REQUEST).build());
                        break;
                    }

                    if (httpRequest.isEmpty()) {
                        LOG.debug("empty line received");
                        break;
                    }

                    if (!handleRequest(httpRequest.get(), response)) {
                        LOG.debug("close connection by header");
                        break;
                    }
                }
            } catch (SocketTimeoutException socketTimeout) {
                LOG.debug("socket timeout", socketTimeout);
                if (httpRequest.map(HttpRequest::getHttpProtocol).orElse(HttpProtocol.HTTP_1) == HttpProtocol.HTTP_1_1) {
                    respondWithTimeout();
                }
            } catch (SocketException ex) {
                // on broken socket close everything
                LOG.debug("socket exception occurred", ex);
            } catch (Exception ex) {
                LOG.error("error during handling of client: ", ex);
            } finally {
                LOG.debug("client connection closed");
                requestBuffer.close();
                responseWriter.close();
                close(clientSocket);
            }
        }

        private void respondWithTimeout() {
            try {
                responseWriter.write(new HttpResponse.Builder(HttpProtocol.HTTP_1_1)
                        .statusCode(HttpStatusCode.REQUEST_TIMEOUT)
                        .build());
            } catch (Exception ex) {
                LOG.error("error during response", ex);
            }
        }

        private void log(HttpRequest httpRequest, HttpResponse httpResponse) {
            LOG.info("{} {} request to uri={} header={} responding with {}",
                    httpRequest.getHttpProtocol().getRepresentation(),
                    httpRequest.getHttpMethod(),
                    httpRequest.getUri(),
                    httpRequest.getHeader(),
                    httpResponse.getStatusCode().getRepresentation());
        }

        private boolean handleRequest(HttpRequest request, HttpResponse.Builder responseBuilder) throws Exception {
            clientRequestProcessor.processRequest(request, responseBuilder);

            final boolean shouldBeKeptAlive = request.getHttpProtocol() == HttpProtocol.HTTP_1_1 && !request.getHeader().isConnectionClose();
            if (shouldBeKeptAlive) {
                responseBuilder.keepAliveFor(keepAliveTimeout);
            }

            final HttpResponse response = responseBuilder.build();
            log(request, response);
            responseWriter.write(response);
            return shouldBeKeptAlive;
        }

        private boolean setupReaderAndWriter() {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                clientSocket.setSoTimeout((int) keepAliveTimeout.toMillis());
                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                responseWriter = new HttpResponseWriter(outputStream, (int) chunkedEncodingBufferSize.toBytes());
                requestBuffer = new HttpRequestBuffer(inputStream);

                clientRequestProcessor = beanFactory.getBean(ClientRequestProcessor.class);

                return true;
            } catch (IOException e) {
                LOG.error("setup of reader and writer failed", e);

                close(inputStream);
                close(outputStream);
                close(clientSocket);
            }
            return false;
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOG.error("error during close", e);
            }
        }
    }
}
