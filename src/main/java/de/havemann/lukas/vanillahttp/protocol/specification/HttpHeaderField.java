package de.havemann.lukas.vanillahttp.protocol.specification;

/**
 * List of common http header fields
 */
public enum HttpHeaderField implements ProtocolRepresentation {

  CONNECTION("Connection"),
  CONTENT_TYPE("ContentType"),
  TRANSFER_ENCODING("Transfer-Encoding"),
  KEEP_ALIVE("Keep-Alive"),
  LAST_MODIFIED("Last-Modified"),

  // RFC 7232

  /**
   * The "ETag" header field in a response provides the current entity-tag for the selected
   * representation, as determined at the conclusion of handling the request.  An entity-tag is an
   * opaque validator for differentiating between multiple representations of the same resource,
   * regardless of whether those multiple representations are due to resource state changes over
   * time, content negotiation resulting in multiple representations being valid at the same time,
   * or both.  An entity-tag consists of an opaque quoted string, possibly prefixed by a weakness
   * indicator.
   */
  E_TAG("ETag"),

  /**
   * The "If-Match" header field makes the request method conditional on the recipient origin server
   * either having at least one current representation of the target resource, when the field-value
   * is "*", or having a current representation of the target resource that has an entity-tag
   * matching a member of the list of entity-tags provided in the field-value.
   */
  IF_MATCH("If-Match"),

  /**
   * The "If-None-Match" header field makes the request method conditional on a recipient cache or
   * origin server either not having any current representation of the target resource, when the
   * field-value is "*", or having a selected representation with an entity-tag that does not match
   * any of those listed in the field-value.
   */
  IF_NONE_MATCH("If-None-Match"),

  /**
   * The "If-Modified-Since" header field makes a GET or HEAD request method conditional on the
   * selected representation's modification date being more recent than the date provided in the
   * field-value. Transfer of the selected representation's data is avoided if that data has not
   * changed.
   */
  IF_MODIFIED_SINCE("If-Modified-Since");

  public static final String KEY_VALUE_DELIMITER = ": ";

  private final String headerFieldName;

  HttpHeaderField(String headerFieldName) {
    this.headerFieldName = headerFieldName;
  }

  @Override
  public String getRepresentation() {
    return headerFieldName;
  }
}
