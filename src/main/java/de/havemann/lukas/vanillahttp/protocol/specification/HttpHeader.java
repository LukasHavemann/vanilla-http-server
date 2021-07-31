package de.havemann.lukas.vanillahttp.protocol.specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * encapsulates access to HTTP header attributes
 */
public class HttpHeader {

  private final Map<String, String> httpHeader;

  public HttpHeader(Map<String, String> httpHeader) {
    this.httpHeader = Collections.unmodifiableMap(httpHeader);
  }

  public Optional<String> getValueOf(HttpHeaderField headerField) {
    return Optional.ofNullable(httpHeader.get(headerField.getRepresentation()));
  }

  public boolean isConnectionClose() {
    return "close".equals(this.getValueOf(HttpHeaderField.CONNECTION).map(String::trim).orElse(""));
  }

  public Stream<String> getValues(HttpHeaderField headerField) {
    return this.getValueOf(headerField)
        .map(v -> v.split(",")).stream().flatMap(Arrays::stream)
        .map(String::trim);
  }

  public Map<String, String> getAll() {
    return this.httpHeader;
  }

  public boolean has(HttpHeaderField headerField) {
    return httpHeader.containsKey(headerField.getRepresentation());
  }

  @Override
  public String toString() {
    // for testing purpose only
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
  }

  @Override
  public int hashCode() {
    // for testing purpose only
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object other) {
    // for testing purpose only
    return EqualsBuilder.reflectionEquals(this, other);
  }
}
