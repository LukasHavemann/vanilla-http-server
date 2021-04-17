package de.havemann.lukas.vanillahttp.protocol.response;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HttpResponseWriterTest {

    private static final String EXPECTED_CHUNKED_RESPONSE = String.join("\r\n",
            "HTTP/1.1 200 OK",
            "ETag: SOMETHING",
            "Connection: keep-alive",
            "Keep-Alive: timeout=10",
            "Transfer-Encoding: chunked",
            "",
            "c",
            "Hello World!",
            "0",
            "",
            "");

    private static final String EXPECTED_NO_CONTENT_RESPONSE = String.join("\r\n",
            "HTTP/1.1 200 OK",
            "Connection: keep-alive",
            "Keep-Alive: timeout=10",
            "",
            "");

    @Test
    public void httpResponseOkWriterTest() throws IOException {
        final HttpResponseHeader.Builder responseHeader = new HttpResponseHeader.Builder()
                .protocol(HttpProtocol.HTTP_1_1)
                .statusCode(HttpStatusCode.OK);

        final ByteArrayOutputStream actual = new ByteArrayOutputStream();

        // Act
        new HttpResponseWriter(actual).header(responseHeader).finish();

        assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_NO_CONTENT_RESPONSE);
    }

    @Test
    public void httpResponseWithChunkedEncodingWriterTest() throws IOException {
        final HttpResponseHeader.Builder responseHeader = new HttpResponseHeader.Builder()
                .protocol(HttpProtocol.HTTP_1_1)
                .statusCode(HttpStatusCode.OK)
                .add(HttpHeaderField.E_TAG, "SOMETHING");

        final ByteArrayOutputStream actual = new ByteArrayOutputStream();
        final ByteArrayInputStream contentToStream = new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8));

        // Act
        new HttpResponseWriter(actual)
                .header(responseHeader)
                .renderChunked(contentToStream)
                .finish();

        assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_CHUNKED_RESPONSE);
    }
}