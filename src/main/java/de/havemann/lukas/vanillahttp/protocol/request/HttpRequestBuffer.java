package de.havemann.lukas.vanillahttp.protocol.request;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HttpRequestBuffer} reads lines from inputStream until a complete http request could be
 * read.
 */
public class HttpRequestBuffer implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(HttpRequestBuffer.class);

  final private BufferedReader reader;
  private StringBuilder buffer = new StringBuilder(512);

  public HttpRequestBuffer(InputStream inputStream) {
    reader = new BufferedReader(new InputStreamReader(inputStream));
  }

  public Optional<HttpRequest> readRequest() throws IOException {
    readUntilEndOfHttpHeader();

    String httpRequest = buffer.toString();
    if (LOG.isDebugEnabled()) {
      LOG.debug("received http request " + httpRequest);
    }

    if (httpRequest.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(new HttpRequestParser(httpRequest).parse());
    } catch (HttpRequestParsingException ex) {
      LOG.error("Can't parse http request. got " + buffer, ex);
      throw ex;
    }
  }

  private void readUntilEndOfHttpHeader() throws IOException {
    buffer = new StringBuilder(512);
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
