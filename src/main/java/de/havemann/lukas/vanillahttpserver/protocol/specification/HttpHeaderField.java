package de.havemann.lukas.vanillahttpserver.protocol.specification;

/**
 * list of http header fields that are explicitly handled
 */
public enum HttpHeaderField implements ProtocolRepresentation {

    CONNECTION("Connection"),
    CONTENT_TYPE("ContentType"),

    E_TAG("ETag"),
    IF_MATCH("If-Match"),
    IF_NONE_MATCH("If-None-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since");

    public static final String KEY_VALUE_DELIMITER = ": ";

    private final String headerFieldName;

    HttpHeaderField(String headerFieldName) {
        this.headerFieldName = headerFieldName;
    }

    public String getHeaderFieldName() {
        return headerFieldName;
    }
}
