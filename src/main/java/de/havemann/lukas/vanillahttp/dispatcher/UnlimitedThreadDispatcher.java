package de.havemann.lukas.vanillahttp.dispatcher;

import de.havemann.lukas.vanillahttp.protocol.request.HttpRequest;
import de.havemann.lukas.vanillahttp.protocol.request.HttpRequestBuffer;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponseHeader;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponseWriter;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@ConfigurationProperties("vanilla.server.http")
public class UnlimitedThreadDispatcher implements ClientConnectionDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(UnlimitedThreadDispatcher.class);

    private final AtomicInteger id = new AtomicInteger(1);
    private final BeanFactory beanFactory;

    @SuppressWarnings("FieldMayBeFinal")
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration keepAliveTimeout = Duration.ofSeconds(10);

    public UnlimitedThreadDispatcher(@Autowired BeanFactory beanFactory) {
        this.beanFactory = Objects.requireNonNull(beanFactory);
    }

    public boolean dispatch(Socket clientSocket) {
        new ClientConnectionHandlerThread(clientSocket).start();
        return true;
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

            try {
                while (!clientSocket.isClosed()) {
                    Optional<HttpRequest> httpRequest = requestBuffer.readRequest();
                    LOG.info("got request {}", httpRequest);

                    if (httpRequest.isEmpty()) {
                        responseWriter.header(new HttpResponseHeader.Builder()
                                .protocol(HttpProtocol.HTTP_1_1)
                                .statusCode(HttpStatusCode.BAD_REQUEST))
                                .finish();
                        continue;
                    }

                    clientRequestProcessor.processRequest(httpRequest.get());

                    if (!httpRequest.get().isKeepAlive()) {
                        break;
                    }
                }
            } catch (SocketTimeoutException socketTimeout) {
                try {
                    responseWriter
                            .header(new HttpResponseHeader.Builder().statusCode(HttpStatusCode.NOT_FOUND))
                            .finish();

                } catch (IOException e) {
                    LOG.error("error during response", e);
                }
            } catch (Exception e) {
                LOG.error("error during handling of client: ", e);
            } finally {
                requestBuffer.close();
                responseWriter.close();
                close(clientSocket);
            }
        }

        private boolean setupReaderAndWriter() {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                clientSocket.setSoTimeout((int) keepAliveTimeout.toMillis());
                inputStream = clientSocket.getInputStream();
                outputStream = clientSocket.getOutputStream();

                responseWriter = new HttpResponseWriter(outputStream);
                requestBuffer = new HttpRequestBuffer(inputStream);

                clientRequestProcessor = beanFactory.getBean(ClientRequestProcessor.class, responseWriter);
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
