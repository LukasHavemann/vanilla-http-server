package de.havemann.lukas.vanillahttp.protocol.response;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.etag.ETag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * tests for {@link HttpResponseWriter}
 */
class HttpResponseWriterTest {

    private static final String EXPECTED_CHUNKED_RESPONSE = String.join(HttpProtocol.DELIMITER,
            "HTTP/1.1 200 OK",
            "Connection: keep-alive",
            "Keep-Alive: timeout=10",
            "ETag: \"SOMETHING\"",
            "Last-Modified: Sun, 31 Oct 2021 10:00:00 GMT",
            "Transfer-Encoding: chunked",
            "",
            "c",
            "Hello World!",
            "0",
            "",
            "");

    private static final String EXPECTED_NO_CONTENT_RESPONSE = String.join(HttpProtocol.DELIMITER,
            "HTTP/1.1 200 OK",
            "Connection: keep-alive",
            "Keep-Alive: timeout=10",
            "",
            "");

    @Test
    void httpResponseOkWriterTest() throws Exception {
        final HttpResponse responseHeader = new HttpResponse.Builder()
                .protocol(HttpProtocol.HTTP_1_1)
                .statusCode(HttpStatusCode.OK)
                .keepAliveFor(Duration.ofSeconds(10))
                .build();

        final ByteArrayOutputStream actual = new ByteArrayOutputStream();

        // Act
        new HttpResponseWriter(actual).write(responseHeader);

        assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_NO_CONTENT_RESPONSE);
    }

    @Test
    void httpResponseWithChunkedEncodingWriterTest() throws Exception {
        final HttpResponse responseHeader = new HttpResponse.Builder()
                .protocol(HttpProtocol.HTTP_1_1)
                .statusCode(HttpStatusCode.OK)
                .keepAliveFor(Duration.ofSeconds(10))
                .eTag(new ETag("SOMETHING", ETag.Kind.STRONG))
                .lastModified(ZonedDateTime.of(LocalDateTime.of(2021, 10, 31, 10, 0), ZoneOffset.UTC))
                .payloadRenderer(() -> new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8)))
                .build();

        final ByteArrayOutputStream actual = new ByteArrayOutputStream();

        // Act
        new HttpResponseWriter(actual).write(responseHeader);

        assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_CHUNKED_RESPONSE);
    }
}