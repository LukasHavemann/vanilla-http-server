package de.havemann.lukas.vanillahttp.acceptancetest;

import de.havemann.lukas.vanillahttp.VanillaHttpServer;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the fulfilment of the following requirements:
 * <p>
 * Add proper HTTP/1.1 keep-alive behavior to your implementation based on the http-client's
 * capabilities exposed through its request headers.
 */
@SpringBootTest(classes = VanillaHttpServer.class)
@TestPropertySource(properties = {
        "vanilla.server.port=9997",
        "vanilla.server.http.keepAliveTimeout: 500ms",
        "vanilla.server.filesystem.basedir=./src/test/resources/sampledirectory"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Extension2AcceptanceTest {

    private SimpleHttpTestClient client;

    @BeforeEach
    void beforeEach() {
        client = new SimpleHttpTestClient(9997);
    }

    @AfterEach
    void afterEach() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    @Test
    void connectionStaysOpen() throws IOException {
        assertThat(client.sendHeadRequest().readResponse()).contains(HttpStatusCode.OK.getRepresentation());
        assertThat(client.sendHeadRequest().readResponse()).contains(HttpStatusCode.OK.getRepresentation());
        assertThat(client.sendHeadRequest().readResponse()).contains(HttpStatusCode.OK.getRepresentation());
    }

    @Test
    void connectionClosesAfterClientTimeout() throws IOException, InterruptedException {
        assertThat(client.sendHeadRequest().readResponse()).contains(HttpStatusCode.OK.getRepresentation());
        TimeUnit.SECONDS.sleep(1);
        assertThat(client.readResponse()).contains(HttpStatusCode.REQUEST_TIMEOUT.getRepresentation());
    }

    @Test
    void handlesInvalidHttpGraceful() throws IOException {
        assertThat(client.send("HEAD / GarbageHTTP/1.1").readResponse()).contains(HttpStatusCode.BAD_REQUEST.getRepresentation());
    }

    @Test
    void handlesHttp1Correctly() throws IOException {
        assertThat(client.send("HEAD / HTTP/1.0").readResponse()).contains(HttpStatusCode.OK.getRepresentation());
        try {
            assertThat(client.send("HEAD / HTTP/1.0").readResponse()).isEqualTo("");
        } catch (SocketException ignored) {
        }
    }

    @Test
    void respectsClientConnectionCloseHeader() throws IOException {
        final String response = client.header("Connection", "close")
                .send("HEAD / HTTP/1.1")
                .readResponse();

        assertThat(response).contains(HttpStatusCode.OK.getRepresentation());
        try {
            assertThat(client.send("HEAD / HTTP/1.0").readResponse()).isEqualTo("");
        } catch (SocketException ignored) {
        }
    }

    static class SimpleHttpTestClient {

        private final int port;
        private Socket clientSocket;
        private DataOutputStream dataOutputStream;
        private BufferedReader reader;
        private String headerValue = "";

        public SimpleHttpTestClient(int port) {
            this.port = port;
        }

        private void initSocket() throws IOException {
            if (clientSocket != null) {
                return;
            }

            this.clientSocket = new Socket("127.0.0.1", port);
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public SimpleHttpTestClient sendHeadRequest() throws IOException {
            return send("HEAD / HTTP/1.1");
        }

        public SimpleHttpTestClient send(String string) throws IOException {
            initSocket();
            dataOutputStream.write((string + "\r\n").getBytes(StandardCharsets.UTF_8));
            if (!headerValue.isEmpty()) {
                dataOutputStream.write(headerValue.getBytes(StandardCharsets.UTF_8));
            }
            dataOutputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
            return this;
        }

        public String readResponse() throws IOException {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                builder.append(line).append("\r\n");
            }
            return builder.toString();
        }

        public void close() throws IOException {
            clientSocket.close();
        }

        public SimpleHttpTestClient header(String key, String value) {
            this.headerValue += (key + ": " + value + "\r\n");
            return this;
        }
    }
}
