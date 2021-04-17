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

public class HttpResponseWriter implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResponseWriter.class);

    private final OutputStream outputStream;

    private HttpResponseHeader.Builder headerBuilder;
    private boolean writeHeaderCalled;
    private int chunkedEncodingBufferSize = 255;

    public HttpResponseWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public HttpResponseWriter header(HttpResponseHeader.Builder headerBuilder) {
        this.headerBuilder = headerBuilder
                .protocol(HttpProtocol.HTTP_1_1)
                .add(HttpHeaderField.CONNECTION, "keep-alive")
                .add(HttpHeaderField.KEEP_ALIVE, "timeout=10");

        return this;
    }

    private void writeHeader() throws IOException {
        if (writeHeaderCalled) {
            throw new IllegalStateException("header already written. response must be finished first.");
        }

        writeHeaderCalled = true;

        final HttpResponseHeader header = headerBuilder.build();
        outputStream.write(header.getProtocol().asUTF8Bytes());
        outputStream.write(' ');
        outputStream.write(header.getStatusCode().asUTF8Bytes());
        writeCRLF();

        for (Pair<HttpHeaderField, String> headerField : header.getHeaderFields()) {
            outputStream.write(headerField.getKey().asUTF8Bytes());
            outputStream.write(HttpHeaderField.KEY_VALUE_DELIMITER.getBytes(StandardCharsets.UTF_8));
            outputStream.write(headerField.getValue().getBytes(StandardCharsets.UTF_8));
            writeCRLF();
        }
    }

    public HttpResponseWriter finish() throws IOException {
        if (!writeHeaderCalled) {
            writeHeader();
        }

        writeCRLF();

        // reset writer for next response
        writeHeaderCalled = false;
        outputStream.flush();
        return this;
    }

    public HttpResponseWriter renderChunked(InputStream inputStream) throws IOException {
        headerBuilder.add(HttpHeaderField.TRANSFER_ENCODING, "chunked");

        writeHeader();

        writeCRLF();
        writeOutChunked(inputStream);
        writeFinalByte();

        return this;
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
