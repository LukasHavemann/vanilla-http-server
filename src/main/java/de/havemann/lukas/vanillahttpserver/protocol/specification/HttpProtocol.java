package de.havemann.lukas.vanillahttpserver.protocol.specification;

/**
 * List of supported HTTP protocols
 */
public enum HttpProtocol implements ProtocolRepresentation {
    HTTP_1("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");

    public static final String DELIMITER = "\r\n";

    private final String protocol;

    @Override
    public String getRepresentation() {
        return protocol;
    }

    HttpProtocol(String protocol) {
        this.protocol = protocol;
    }
}
