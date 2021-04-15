package de.havemann.lukas.vanillahttpserver.protocol.request;

import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpRequestBuffer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestBuffer.class);

    final private StringBuilder buffer = new StringBuilder(512);

    public HttpRequest consume(InputStream stream) throws IOException {
        readUntilEndOfHttpHeader(stream);

        String httpRequest = buffer.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("received http request " + httpRequest);
        }

        try {
            return new HttpRequestParser(httpRequest).parse();
        } catch (HttpRequestParsingException ex) {
            LOG.error("error during parsing of HTTP request. request was:\n{}", httpRequest);
            throw ex;
        }
    }

    private void readUntilEndOfHttpHeader(InputStream stream) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while (!(line = reader.readLine()).isBlank()) {
            buffer.append(line).append(HttpProtocol.DELIMITER);
        }
    }

}
