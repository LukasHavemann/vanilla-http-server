package de.havemann.lukas.vanillahttpserver.acceptancetest;


import de.havemann.lukas.vanillahttpserver.VanillaHttpServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
@SpringBootTest(classes = VanillaHttpServer.class)
@TestPropertySource(properties = "vanilla.server.port=9999")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmokeTest {

    public static final String BASE_URL = "http://localhost:9999/";
    private OkHttpClient client;

    @BeforeAll
    public void setup() {
        client = new OkHttpClient();
    }

    @Test
    public void simpleGetSmokeTest() throws IOException {
        final Document document = Jsoup.connect(BASE_URL).get();

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(document.select("li")).hasSize(9);
        softly.assertAll();
    }

    @Test
    public void simpleHeadSmokeTest() throws IOException {
        final Response response = exchange(getBaseUrl().head());

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.code()).isEqualTo(200);
        softly.assertThat(response.body().string()).isBlank();
        softly.assertAll();
    }

    private Response exchange(Request.Builder request) {
        try {
            return client.newCall(request.build()).execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private Request.Builder getBaseUrl() {
        return new Request.Builder().url(BASE_URL);
    }
}
