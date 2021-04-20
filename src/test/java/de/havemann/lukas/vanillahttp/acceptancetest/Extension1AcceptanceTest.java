package de.havemann.lukas.vanillahttp.acceptancetest;

import de.havemann.lukas.vanillahttp.VanillaHttpServer;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the fulfilment of the following requirements:
 * <p>
 * Add proper handling of HTTP ETag, If-Match, If-None-Match, If-Modified-Since headers.
 */
@SpringBootTest(classes = VanillaHttpServer.class)
@TestPropertySource(properties = {
        "vanilla.server.port=9998",
        "vanilla.server.filesystem.basedir=./src/test/resources/sampledirectory"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Extension1AcceptanceTest {

    private static final String BASE_URL = "http://localhost:9998/";

    private static final ZonedDateTime LAST_MODIFIED_OF_TEST_FILE =
            ZonedDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 15 Apr 2021 18:26:56 GMT"));

    @Test
    void ifLastModifiedIsRespectedTest() throws IOException {
        final Connection connection = Jsoup.connect(BASE_URL + "fileonfirstlevel.txt").method(Connection.Method.GET);
        final Connection.Response firstResponse = connection.execute();
        final String firstResponseLastModified = firstResponse.header(HttpHeaderField.LAST_MODIFIED.getRepresentation());

        assertThat(firstResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());

        final Connection.Response secondResponse = connection
                .header(HttpHeaderField.IF_MODIFIED_SINCE.getRepresentation(), firstResponseLastModified)
                .execute();

        assertThat(secondResponse.statusCode()).isEqualTo(HttpStatusCode.NOT_MODIFIED.getCode());
    }

    @Test
    void ifLastModifiedIsRespectedOnHeadRequestTooTest() throws IOException {
        final Connection connection = Jsoup.connect(BASE_URL + "fileonfirstlevel.txt").method(Connection.Method.HEAD);
        final Connection.Response firstResponse = connection.execute();
        final String firstResponseLastModified = firstResponse.header(HttpHeaderField.LAST_MODIFIED.getRepresentation());

        assertThat(firstResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());

        final Connection.Response secondResponse = connection
                .header(HttpHeaderField.IF_MODIFIED_SINCE.getRepresentation(), firstResponseLastModified)
                .execute();

        assertThat(secondResponse.statusCode()).isEqualTo(HttpStatusCode.NOT_MODIFIED.getCode());
    }

    @Test
    void oldLastModifiedTest() throws IOException {
        final Connection connection = Jsoup.connect(BASE_URL + "fileonfirstlevel.txt").method(Connection.Method.GET);
        final Connection.Response firstResponse = connection.execute();

        assertThat(firstResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());

        final Connection.Response secondResponse = connection
                .header(HttpHeaderField.IF_MODIFIED_SINCE.getRepresentation(),
                        DateTimeFormatter.RFC_1123_DATE_TIME.format(LAST_MODIFIED_OF_TEST_FILE.minusDays(10)))
                .execute();

        assertThat(secondResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());
    }

    @Test
    void ifMatchIsRespectedTest() throws IOException {
        final Connection connection = Jsoup.connect(BASE_URL + "fileonfirstlevel.txt").method(Connection.Method.GET);
        final Connection.Response firstResponse = connection.execute();
        final String firstResponseEtag = firstResponse.header(HttpHeaderField.E_TAG.getRepresentation());

        assertThat(firstResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());

        final Connection.Response secondResponse = connection
                .header(HttpHeaderField.IF_MATCH.getRepresentation(), firstResponseEtag)
                .execute();

        assertThat(secondResponse.statusCode()).isEqualTo(HttpStatusCode.NOT_MODIFIED.getCode());

        final Connection.Response thirdResponse = connection
                .header(HttpHeaderField.IF_MATCH.getRepresentation(), "wrongETag")
                .execute();

        assertThat(thirdResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());
    }


    @Test
    void eTagShouldHavePrecedenceBeforeLastModifiedTest() throws IOException {
        final Connection connection = Jsoup.connect(BASE_URL + "fileonfirstlevel.txt").method(Connection.Method.GET);
        final Connection.Response firstResponse = connection.execute();
        final String firstResponseEtag = firstResponse.header(HttpHeaderField.E_TAG.getRepresentation());

        assertThat(firstResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());

        final Connection.Response secondResponse = connection
                .header(HttpHeaderField.IF_MATCH.getRepresentation(), firstResponseEtag)
                .header(HttpHeaderField.LAST_MODIFIED.getRepresentation(),
                        DateTimeFormatter.RFC_1123_DATE_TIME.format(LAST_MODIFIED_OF_TEST_FILE.plusDays(10)))
                .execute();

        assertThat(secondResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());
    }

    @Test
    void ifNoneMatchIsRespectedTest() throws IOException {
        final Connection connection = Jsoup.connect(BASE_URL + "fileonfirstlevel.txt").method(Connection.Method.GET);
        final Connection.Response firstResponse = connection.execute();
        final String firstResponseEtag = firstResponse.header(HttpHeaderField.E_TAG.getRepresentation());

        assertThat(firstResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());

        final Connection.Response secondResponse = connection
                .header(HttpHeaderField.IF_NONE_MATCH.getRepresentation(), firstResponseEtag)
                .execute();

        assertThat(secondResponse.statusCode()).isEqualTo(HttpStatusCode.NOT_MODIFIED.getCode());

        final Connection.Response thirdResponse = connection
                .header(HttpHeaderField.IF_NONE_MATCH.getRepresentation(), "wrongETag")
                .execute();

        assertThat(thirdResponse.statusCode()).isEqualTo(HttpStatusCode.OK.getCode());
    }
}
