package de.havemann.lukas.vanillahttp.protocol.request;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;

public class HttpRequestBuffer implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestBuffer.class);

    final private StringBuilder buffer = new StringBuilder(512);
    final private BufferedReader reader;

    public HttpRequestBuffer(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public Optional<HttpRequest> readRequest() throws IOException {
        readUntilEndOfHttpHeader();

        String httpRequest = buffer.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("received http request " + httpRequest);
        }

        try {
            return Optional.of(new HttpRequestParser(httpRequest).parse());
        } catch (HttpRequestParsingException ex) {
            LOG.error("Can't parse http request. got " + buffer, ex);
        }
        return Optional.empty();
    }

    private void readUntilEndOfHttpHeader() throws IOException {
        try {
            String line;
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                buffer.append(line).append(HttpProtocol.DELIMITER);
            }
        } catch (IOException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("received so far:\n" + buffer.toString());
            }
            throw ex;
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            LOG.error("error during close", e);
        }
    }
}
