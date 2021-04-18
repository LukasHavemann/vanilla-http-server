package de.havemann.lukas.vanillahttp.protocol.request;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpMethod;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * tests for {@link HttpRequestParser}
 */
class HttpRequestParserTest {

    private static final String EXAMPLE_GET_REQUEST = String.join(HttpProtocol.DELIMITER,
            "GET / HTTP/1.1",
            "Content-Length: 30",
            "Content-Type: */*; charset=UTF-8",
            "Connection: Keep-Alive",
            "User-Agent: Apache-HttpClient/4.5.12 (Java/11.0.9.1)",
            "Accept-Encoding: gzip,deflate");

    private static final String EXAMPLE_HEAD_REQUEST = String.join(HttpProtocol.DELIMITER,
            "HEAD /test/head HTTP/1.1",
            "Accept: application/json",
            "Host: adobe.com");

    @Test
    void parseGetRequestTest() {
        final HttpRequest actual = new HttpRequestParser(EXAMPLE_GET_REQUEST).parse();

        final SoftAssertions softly = new SoftAssertions();

        softly.assertThat(actual.getHttpMethod()).isEqualTo(HttpMethod.GET);
        softly.assertThat(actual.getHttpProtocol()).isEqualTo(HttpProtocol.HTTP_1_1);
        softly.assertThat(actual.getUri()).isEqualTo("/");
        softly.assertThat(actual.getHeader().getAll()).containsKey("Content-Type").hasSize(5);
        softly.assertThat(actual.getHeader().getValueOf(HttpHeaderField.CONNECTION)).isNotEmpty();
        softly.assertThat(actual.getMessageBody()).isEmpty();

        softly.assertAll();
    }

    @Test
    void parseHeadRequestTest() {
        final HttpRequest actual = new HttpRequestParser(EXAMPLE_HEAD_REQUEST).parse();

        final SoftAssertions softly = new SoftAssertions();

        softly.assertThat(actual.getHttpMethod()).isEqualTo(HttpMethod.HEAD);
        softly.assertThat(actual.getHttpProtocol()).isEqualTo(HttpProtocol.HTTP_1_1);
        softly.assertThat(actual.getUri()).isEqualTo("/test/head");
        softly.assertThat(actual.getMessageBody()).isEmpty();

        softly.assertAll();
    }

    @Test
    void parseErrorUri() {
        assertThatThrownBy(() -> new HttpRequestParser("HEAD ").parse())
                .isInstanceOf(HttpRequestParsingException.class)
                .extracting(e -> ((HttpRequestParsingException) e).getReason())
                .isEqualTo(HttpRequestParsingException.Reason.URI_EXPECTED);
    }

    @Test
    void parseErrorHttpProtocolExpected() {
        assertThatThrownBy(() -> new HttpRequestParser("HEAD /").parse())
                .isInstanceOf(HttpRequestParsingException.class)
                .extracting(e -> ((HttpRequestParsingException) e).getReason())
                .isEqualTo(HttpRequestParsingException.Reason.HTTP_PROTOCOL_EXPECTED);
    }

    @Test
    void parseErrorHttpProtocolUnsupported() {
        assertThatThrownBy(() -> new HttpRequestParser("HEAD / HTTP/2.0").parse())
                .isInstanceOf(HttpRequestParsingException.class)
                .extracting(e -> ((HttpRequestParsingException) e).getReason())
                .isEqualTo(HttpRequestParsingException.Reason.UNSUPPORTED_HTTP_PROTOCOL);
    }
}
