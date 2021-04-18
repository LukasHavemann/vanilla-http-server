package de.havemann.lukas.vanillahttp.acceptancetest;

import de.havemann.lukas.vanillahttp.VanillaHttpServer;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import org.assertj.core.api.SoftAssertions;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

@SpringBootTest(classes = VanillaHttpServer.class)
@TestPropertySource(properties = {
        "vanilla.server.port=9997",
        "vanilla.server.http.keepAliveTimeout: 300ms",
        "vanilla.server.filesystem.basedir=./src/test/resources/sampledirectory"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Extension2AcceptanceTest {

    public static final String BASE_URL = "http://localhost:9997/";

    @Test
    void connectionStaysOpen() throws IOException {
        // TODO: 18.04.21 low level client needed

        final Connection connection = Jsoup.connect(BASE_URL).header("Connection", "keep-alive").method(Connection.Method.GET);

        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(executeRequest(connection)).as("fst request").isEqualTo(HttpStatusCode.OK.getCode());
        softly.assertThat(executeRequest(connection)).as("scd request").isEqualTo(HttpStatusCode.OK.getCode());
        softly.assertAll();
    }

    @Test
    void connectionClosesAfterClientTimeoutOpen() throws IOException, InterruptedException {
        // TODO: 18.04.21 low level client needed
    }

    private int executeRequest(Connection connection) throws IOException {
        try {
            return connection.execute().statusCode();
        } catch (org.jsoup.HttpStatusException ex) {
            return ex.getStatusCode();
        }
    }
}
