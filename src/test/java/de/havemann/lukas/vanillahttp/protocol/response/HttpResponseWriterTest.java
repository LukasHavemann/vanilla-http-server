package de.havemann.lukas.vanillahttp.protocol.response;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.etag.ETag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  private static final String EXPECTED_HTTP1_RESPONSE = String.join(HttpProtocol.DELIMITER,
      "HTTP/1.0 200 OK",
      "",
      "Hello World!",
      "",
      "");

  private static final String EXPECTED_NO_CONTENT_RESPONSE = String.join(HttpProtocol.DELIMITER,
      "HTTP/1.1 200 OK",
      "Connection: keep-alive",
      "Keep-Alive: timeout=10",
      "",
      "");

  private ByteArrayOutputStream actual;
  private HttpResponseWriter testee;

  @BeforeEach
  void beforeEach() {
    actual = new ByteArrayOutputStream();
    testee = new HttpResponseWriter(actual);
  }

  @Test
  void httpResponseOkWriterTest() throws Exception {
    final HttpResponse responseHeader = new HttpResponse.Builder(HttpProtocol.HTTP_1_1)
        .statusCode(HttpStatusCode.OK)
        .keepAliveFor(Duration.ofSeconds(10))
        .build();

    testee.write(responseHeader);

    assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_NO_CONTENT_RESPONSE);
  }

  @Test
  void httpResponseWithChunkedEncodingWriterTest() throws Exception {
    final HttpResponse responseHeader = new HttpResponse.Builder(HttpProtocol.HTTP_1_1)
        .statusCode(HttpStatusCode.OK)
        .keepAliveFor(Duration.ofSeconds(10))
        .eTag(new ETag("SOMETHING", ETag.Kind.STRONG))
        .lastModified(ZonedDateTime.of(LocalDateTime.of(2021, 10, 31, 10, 0), ZoneOffset.UTC))
        .payloadRenderer(
            () -> new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8)))
        .build();

    testee.write(responseHeader);

    assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_CHUNKED_RESPONSE);
  }

  @Test
  void http1ResponseWriterTest() throws Exception {
    final HttpResponse responseHeader = new HttpResponse.Builder(HttpProtocol.HTTP_1)
        .statusCode(HttpStatusCode.OK)
        .payloadRenderer(
            () -> new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8)))
        .build();

    testee.write(responseHeader);

    assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_HTTP1_RESPONSE);
  }
}