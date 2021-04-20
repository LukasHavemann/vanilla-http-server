package de.havemann.lukas.vanillahttp.protocol.response;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * {@link HttpResponseWriter} wraps a {@link OutputStream}
 */
public class HttpResponseWriter implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResponseWriter.class);
    public static final int DEFAULT_BUFFER_SIZE = 255;

    private final OutputStream outputStream;
    private final int chunkedEncodingBufferSize;

    /**
     * @param outputStream stream to write http protocol output to
     * @param bufferSize   buffer size for chunked encoding in bytes
     */
    public HttpResponseWriter(OutputStream outputStream, int bufferSize) {
        this.outputStream = Objects.requireNonNull(outputStream);
        this.chunkedEncodingBufferSize = bufferSize;
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("invalid buffer size of " + bufferSize);
        }
    }

    /**
     * Creates new instance with default buffer size of {@link #DEFAULT_BUFFER_SIZE}
     *
     * @param outputStream stream to write http protocol output to
     */
    public HttpResponseWriter(OutputStream outputStream) {
        this(outputStream, DEFAULT_BUFFER_SIZE);
    }

    public void write(HttpResponse httpResponse) throws Exception {
        writeHeader(httpResponse);

        if (httpResponse.getPayloadRenderer().isPresent()) {
            renderBody(httpResponse.getProtocol(), httpResponse.getPayloadRenderer().get());
        }

        finish();
    }

    private void writeHeader(HttpResponse httpResponse) throws IOException {
        outputStream.write(httpResponse.getProtocol().asUTF8Bytes());
        outputStream.write(' ');
        outputStream.write(httpResponse.getStatusCode().asUTF8Bytes());
        writeCRLF();

        for (Pair<HttpHeaderField, String> headerField : httpResponse.getHeaderFields()) {
            outputStream.write(headerField.getKey().asUTF8Bytes());
            outputStream.write(HttpHeaderField.KEY_VALUE_DELIMITER.getBytes(StandardCharsets.UTF_8));
            outputStream.write(headerField.getValue().getBytes(StandardCharsets.UTF_8));
            writeCRLF();
        }
    }

    private void finish() throws IOException {
        writeCRLF();
        outputStream.flush();
    }

    private void renderBody(HttpProtocol protocol, Callable<InputStream> inputStreamSupplier) throws Exception {
        try (InputStream inputStream = inputStreamSupplier.call()) {
            selectByProtocol(protocol, inputStream);
        }
    }

    private void selectByProtocol(HttpProtocol protocol, InputStream inputStream) throws IOException {
        writeCRLF();

        if (protocol == HttpProtocol.HTTP_1) {
            inputStream.transferTo(outputStream);
            writeCRLF();
            return;
        }

        if (protocol == HttpProtocol.HTTP_1_1) {
            writeOutChunked(inputStream);
            writeFinalByte();
            return;
        }

        throw new IllegalArgumentException("not supported " + protocol);
    }

    private void writeFinalByte() throws IOException {
        outputStream.write('0');
        writeCRLF();
    }

    private void writeOutChunked(InputStream inputStream) throws IOException {
        byte[] buffer;
        do {
            buffer = inputStream.readNBytes(chunkedEncodingBufferSize);
            if (buffer.length == 0) {
                return;
            }

            outputStream.write(Integer.toHexString(buffer.length).getBytes(StandardCharsets.UTF_8));
            writeCRLF();
            outputStream.write(buffer);
            writeCRLF();
        } while (inputStream.available() != 0);
    }

    private void writeCRLF() throws IOException {
        outputStream.write('\r');
        outputStream.write('\n');
    }

    @Override
    public void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            LOG.error("error during close", e);
        }
    }
}
