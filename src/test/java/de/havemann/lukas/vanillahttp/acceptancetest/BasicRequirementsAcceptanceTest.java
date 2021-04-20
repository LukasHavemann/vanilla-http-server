package de.havemann.lukas.vanillahttp.acceptancetest;


import de.havemann.lukas.vanillahttp.VanillaHttpServer;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testing the fulfilment of the following requirements:
 * <p>
 * The server should serve static files and directories from a user-specified root directory.
 * - It should be possible to discover subdirectories as well.
 * - The server must at least handle HTTP GET and HEAD requests.
 */
@SpringBootTest(classes = VanillaHttpServer.class)
@TestPropertySource(properties = {
        "vanilla.server.port=9999",
        "vanilla.server.filesystem.basedir=./src/test/resources/sampledirectory"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicRequirementsAcceptanceTest {

    private static final String BASE_URL = "http://localhost:9999/";

    /**
     * Server must handle get request and its is possible to discover subdirectories
     */
    @Test
    void httpGetRequestToBaseDirTest() throws IOException {
        final Document document = Jsoup.connect(BASE_URL).method(Connection.Method.GET).execute().parse();

        assertThat(document.select("li").stream().map(Element::text).collect(Collectors.toList()))
                .hasSize(4)
                .contains("dirwithhtml/")
                .contains("subdirectory/");
    }

    /**
     * Server must prevent access outside of base dir.
     */
    @Test
    void httpGetOutsideFromBaseDirIsPreventedTest() throws IOException {
        try {
            Jsoup.connect(BASE_URL + "../../").method(Connection.Method.GET).execute();
            fail("jsoup should have thrown exception");
        } catch (org.jsoup.HttpStatusException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(403);
        }
    }

    /**
     * Server must handle head request
     */
    @Test
    void httpHeadRequestToBaseDirTest() throws IOException {
        final Connection.Response response = Jsoup.connect(BASE_URL).method(Connection.Method.HEAD).execute();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).isEqualTo(200);
        softly.assertThat(response.body()).isBlank();
        softly.assertAll();
    }

    /**
     * Server should be able to handle sub directories and files with spaces
     */
    @Test
    void httpGetRequestToSubdirectoryWithSpacesTest() throws IOException {
        final Connection.Response response = Jsoup.connect(BASE_URL + "sub%20with%20space/and%20file.txt")
                .method(Connection.Method.GET)
                .execute();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.header("ContentType")).isEqualTo("text/plain");
        softly.assertThat(response.body()).isEqualTo("file with spaces");
        softly.assertAll();
    }

    @Test
    void httpGetRequestToJpgTest() throws IOException {
        final Connection.Response response = Jsoup.connect(BASE_URL + "subdirectory/hamburg.jpg")
                .method(Connection.Method.GET)
                .execute();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.header("ContentType")).isEqualTo("image/jpeg");
        softly.assertThat(response.body()).contains("https://flickr.com/");
        softly.assertAll();
    }

    /**
     * Server must handle get request and its is possible to discover subdirectories
     */
    @Test
    void httpGetToDirectoryWithIndexFileIsDisplayed() throws IOException {
        final Connection.Response response = Jsoup.connect(BASE_URL + "dirwithhtml").method(Connection.Method.GET).execute();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.parse().select("h1").text()).isEqualTo("Welcome to the sample site");
        softly.assertThat(response.header("ContentType")).isEqualTo("text/html");
        softly.assertAll();
    }

    /**
     * Server must handle request to unknown files
     */
    @Test
    void httpGetRequestToUnknownFile() throws IOException {
        try {
            Jsoup.connect(BASE_URL + "unknown").method(Connection.Method.GET).execute();

            fail("jsoup should have thrown exception");
        } catch (org.jsoup.HttpStatusException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(404);
        }
    }
}
