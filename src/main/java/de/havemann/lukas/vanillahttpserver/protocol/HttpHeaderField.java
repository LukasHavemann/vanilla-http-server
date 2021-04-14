package de.havemann.lukas.vanillahttpserver.protocol;

/**
 * list of http header fields that are explicitly handled
 */
public enum HttpHeaderField {

    CONNECTION("Connection"),

    E_TAG("ETag"),
    IF_MATCH("If-Match"),
    IF_NONE_MATCH("If-None-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since");

    private final String headerFieldName;

    HttpHeaderField(String headerFieldName) {
        this.headerFieldName = headerFieldName;
    }

    public String getHeaderFieldName() {
        return headerFieldName;
    }
}
