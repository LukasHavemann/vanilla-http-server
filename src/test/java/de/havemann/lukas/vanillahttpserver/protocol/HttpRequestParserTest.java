package de.havemann.lukas.vanillahttpserver.protocol;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpRequestParserTest {

    private static final String EXAMPLE_GET_REQUEST = String.join("\r\n",
            "GET / HTTP/1.1",
            "Content-Length: 30",
            "Content-Type: */*; charset=UTF-8",
            "Connection: Keep-Alive",
            "User-Agent: Apache-HttpClient/4.5.12 (Java/11.0.9.1)",
            "Accept-Encoding: gzip,deflate");

    private static final String EXAMPLE_HEAD_REQUEST = String.join("\r\n",
            "HEAD /echo/head/json HTTP/1.1",
            "Accept: application/json",
            "Host: reqbin.com");

    @Test
    public void parseGetRequestTest() {
        final HttpRequest actual = new HttpRequestParser(EXAMPLE_GET_REQUEST).parse();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual.getHttpMethod()).isEqualTo(HttpMethod.GET);
        softly.assertThat(actual.getHttpProtocol()).isEqualTo(HttpProtocol.HTTP_1_1);
        softly.assertThat(actual.getUri()).isEqualTo("/");
        softly.assertThat(actual.getHttpHeaders()).containsKey("Content-Type").hasSize(5);
        softly.assertThat(actual.getHttpHeaders().get("Connection")).isEqualTo("Keep-Alive");
        softly.assertThat(actual.getMessageBody()).isEmpty();

        softly.assertAll();
    }

    @Test
    public void parseHeadRequestTest() {
        final HttpRequest actual = new HttpRequestParser(EXAMPLE_HEAD_REQUEST).parse();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual.getHttpMethod()).isEqualTo(HttpMethod.HEAD);
        softly.assertThat(actual.getHttpProtocol()).isEqualTo(HttpProtocol.HTTP_1_1);
        softly.assertThat(actual.getUri()).isEqualTo("/echo/head/json");
        softly.assertThat(actual.getMessageBody()).isEmpty();

        softly.assertAll();
    }
}
