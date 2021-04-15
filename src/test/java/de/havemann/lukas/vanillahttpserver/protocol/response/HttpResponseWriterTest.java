package de.havemann.lukas.vanillahttpserver.protocol.response;

import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpStatusCode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HttpResponseWriterTest {

    private static final String EXPECTED_RESPONSE = String.join("\r\n",
            "HTTP/1.1 200 OK",
            "E_TAG: SOMETHING",
            "",
            "Hello World!",
            "",
            "");

    @Test
    public void httpResponseOkWriterTest() throws IOException {
        final HttpResponseHeader responseHeader = new HttpResponseHeader.Builder()
                .protocol(HttpProtocol.HTTP_1_1)
                .statusCode(HttpStatusCode.OK)
                .add(HttpHeaderField.E_TAG, "SOMETHING")
                .build();

        final ByteArrayOutputStream actual = new ByteArrayOutputStream();
        final ByteArrayInputStream contentToStream = new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8));

        // Act
        new HttpResponseWriter(actual)
                .writeHeader(responseHeader)
                .streamDataFrom(contentToStream)
                .finish();

        assertThat(actual.toString(StandardCharsets.UTF_8)).isEqualTo(EXPECTED_RESPONSE);
    }
}