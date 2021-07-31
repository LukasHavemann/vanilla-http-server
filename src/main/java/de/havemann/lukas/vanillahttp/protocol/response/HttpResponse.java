package de.havemann.lukas.vanillahttp.protocol.response;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;
import de.havemann.lukas.vanillahttp.protocol.specification.etag.ETag;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.tuple.Pair;

public class HttpResponse {

  private final HttpProtocol protocol;
  private final HttpStatusCode statusCode;
  private final List<Pair<HttpHeaderField, String>> headerFields;
  private final Callable<InputStream> payloadRenderer;

  public HttpResponse(Builder builder) {
    this.protocol = Objects.requireNonNull(builder.protocol);
    this.statusCode = Objects.requireNonNull(builder.statusCode);
    this.headerFields = Collections.unmodifiableList(builder.headerFields);
    this.payloadRenderer = builder.payloadRenderer;
  }

  public HttpProtocol getProtocol() {
    return protocol;
  }

  public HttpStatusCode getStatusCode() {
    return statusCode;
  }

  public List<Pair<HttpHeaderField, String>> getHeaderFields() {
    return headerFields;
  }

  public Optional<Callable<InputStream>> getPayloadRenderer() {
    return Optional.ofNullable(payloadRenderer);
  }

  @SuppressWarnings("UnusedReturnValue")
  public static class Builder {

    private final List<Pair<HttpHeaderField, String>> headerFields = new ArrayList<>();
    private final HttpProtocol protocol;
    private HttpStatusCode statusCode;
    private Callable<InputStream> payloadRenderer;

    public Builder(HttpProtocol protocol) {
      this.protocol = Objects.requireNonNull(protocol);
    }

    public Builder statusCode(HttpStatusCode statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public Builder add(HttpHeaderField headerField, String value) {
      headerFields.add(Pair.of(headerField, value));
      return this;
    }

    public Builder keepAliveFor(Duration duration) {
      add(HttpHeaderField.CONNECTION, "keep-alive");
      add(HttpHeaderField.KEEP_ALIVE, "timeout=" + duration.getSeconds());
      return this;
    }

    public Builder contentType(MediaType mediatype) {
      add(HttpHeaderField.CONTENT_TYPE, mediatype.getRepresentation());
      return this;
    }

    public Builder eTag(ETag etag) {
      add(HttpHeaderField.E_TAG, etag.getRepresentation());
      return this;
    }

    public Builder lastModified(ZonedDateTime lastModified) {
      add(HttpHeaderField.LAST_MODIFIED, DateTimeFormatter.RFC_1123_DATE_TIME.format(lastModified));
      return this;
    }

    public Builder payloadRenderer(Callable<InputStream> payloadRenderer) {
      if (protocol == HttpProtocol.HTTP_1_1) {
        add(HttpHeaderField.TRANSFER_ENCODING, "chunked");
      }
      this.payloadRenderer = payloadRenderer;
      return this;
    }

    public HttpResponse build() {
      return new HttpResponse(this);
    }
  }
}
