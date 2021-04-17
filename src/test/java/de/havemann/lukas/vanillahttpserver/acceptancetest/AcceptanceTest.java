package de.havemann.lukas.vanillahttpserver.acceptancetest;


import de.havemann.lukas.vanillahttpserver.VanillaHttpServer;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(classes = VanillaHttpServer.class)
@TestPropertySource(properties = {
        "vanilla.server.port=9999",
        "vanilla.server.basedir=./src/test/resources/sampledirectory"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AcceptanceTest {

    public static final String BASE_URL = "http://localhost:9999/";
    private Connection client;

    @BeforeAll
    public void setup() {
        client = Jsoup.connect(BASE_URL);
    }

    /**
     * Server must handle get request and its is possible to discover subdirectories
     */
    @Disabled
    @Test
    public void httpGetRequestToBaseDirTest() throws IOException {
        final Document document = client.url(BASE_URL).method(Connection.Method.GET).execute().parse();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(document.select("li")).hasSize(4);
        softly.assertThat(document.select("li:first-of-type").text()).isEqualTo("subdirectory/");
        softly.assertAll();
    }


    /**
     * Server must handle head request
     */
    @Disabled
    @Test
    public void httpHeadRequestToBaseDirTest() throws IOException {
        final Connection.Response response = client.method(Connection.Method.HEAD).execute();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).isEqualTo(200);
        softly.assertThat(response.body()).isBlank();
        softly.assertAll();
    }

    /**
     * Server should be able to handle sub directories and files with spaces
     */
    @Test
    public void httpGetRequestToSubdirectoryWithSpacesTest() throws IOException {
        final Connection.Response response = client.url(BASE_URL + "sub%20with%20space/and%20file.txt")
                .method(Connection.Method.GET)
                .execute();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.header("ContentType")).isEqualTo("text/plain");
        softly.assertThat(response.body()).isEqualTo("file with spaces");
        softly.assertAll();
    }

    @Test
    public void httpGetRequestToJpgTest() throws IOException {
        final Connection.Response response = client.url(BASE_URL + "subdirectory/hamburg.jpg")
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
    public void httpGetToDirectoryWithIndexFileIsDisplayed() throws IOException {
        final Connection.Response response = client.url(BASE_URL + "dirwithhtml").method(Connection.Method.GET).execute();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.parse().select("h1").text()).isEqualTo("Welcome to the sample site");
        softly.assertThat(response.header("ContentType")).isEqualTo("text/html");
        softly.assertAll();
    }

    /**
     * Server must handle request to unknown files
     */
    @Test
    public void httpGetRequestToUnknownFile() throws IOException {
        try {
            client.url(BASE_URL + "unknown")
                    .method(Connection.Method.GET)
                    .execute();

            fail("jsoup should have thrown exception");
        } catch (org.jsoup.HttpStatusException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(404);
        }
    }

}
