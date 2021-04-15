package de.havemann.lukas.vanillahttpserver.protocol.response;

import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpProtocol;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponseWriter {

    public static final byte[] SPACE = " ".getBytes(StandardCharsets.UTF_8);

    private final OutputStream stream;

    public HttpResponseWriter(OutputStream stream) {
        this.stream = stream;
    }

    public HttpResponseWriter writeHeader(HttpResponseHeader header) throws IOException {
        stream.write(header.getProtocol().asBytes());
        stream.write(SPACE);
        stream.write(header.getStatusCode().asBytes());
        stream.write(HttpProtocol.DELIMITER.getBytes(StandardCharsets.UTF_8));

        for (Pair<HttpHeaderField, String> headerField : header.getHeaderFields()) {
            stream.write(headerField.getKey().asBytes());
            stream.write(HttpHeaderField.KEY_VALUE_DELIMITER.getBytes(StandardCharsets.UTF_8));
            stream.write(headerField.getValue().getBytes(StandardCharsets.UTF_8));
            stream.write(HttpProtocol.DELIMITER.getBytes(StandardCharsets.UTF_8));
        }

        stream.write(HttpProtocol.DELIMITER.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public HttpResponseWriter streamDataFrom(InputStream inputStream) throws IOException {
        inputStream.transferTo(stream);
        return this;
    }

    public HttpResponseWriter finish() throws IOException {
        stream.write(HttpProtocol.DELIMITER.getBytes(StandardCharsets.UTF_8));
        stream.write(HttpProtocol.DELIMITER.getBytes(StandardCharsets.UTF_8));
        stream.flush();
        return this;
    }
}
