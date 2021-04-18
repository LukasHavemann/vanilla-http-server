package de.havemann.lukas.vanillahttp.acceptancetest;

import de.havemann.lukas.vanillahttp.VanillaHttpServer;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = VanillaHttpServer.class)
@TestPropertySource(properties = {
        "vanilla.server.port=9997",
        "vanilla.server.http.keepAliveTimeout: 500ms",
        "vanilla.server.filesystem.basedir=./src/test/resources/sampledirectory"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Extension2AcceptanceTest {

    @Test
    void connectionStaysOpen() throws IOException {
        final HttpTestClient httpTestClient = new HttpTestClient(9997);

        assertThat(httpTestClient.sendHeadRequest().readResponse()).contains(HttpStatusCode.OK.getRepresentation());
        assertThat(httpTestClient.sendHeadRequest().readResponse()).contains(HttpStatusCode.OK.getRepresentation());

        httpTestClient.close();
    }

    @Test
    void connectionClosesAfterClientTimeoutOpen() throws IOException, InterruptedException {
        final HttpTestClient httpTestClient = new HttpTestClient(9997);

        assertThat(httpTestClient.sendHeadRequest().readResponse()).contains(HttpStatusCode.OK.getRepresentation());
        TimeUnit.SECONDS.sleep(1);
        assertThat(httpTestClient.readResponse()).contains(HttpStatusCode.REQUEST_TIMEOUT.getRepresentation());

        httpTestClient.close();
    }

    @Test
    void handelsInvalidHttpGracefull() throws IOException {
        final HttpTestClient httpTestClient = new HttpTestClient(9997);

        assertThat(httpTestClient.sendGarbageRequest().readResponse()).contains(HttpStatusCode.BAD_REQUEST.getRepresentation());

        httpTestClient.close();
    }

    static class HttpTestClient {

        private final Socket clientSocket;
        private final DataOutputStream dataOutputStream;
        private final BufferedReader reader;

        public HttpTestClient(int port) throws IOException {
            this.clientSocket = new Socket("127.0.0.1", port);
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public HttpTestClient sendHeadRequest() throws IOException {
            dataOutputStream.write("HEAD / HTTP/1.1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            return this;
        }

        public HttpTestClient sendGarbageRequest() throws IOException {
            dataOutputStream.write("HEAD / GarbageHTTP/1.1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
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
    }
}
