package de.havemann.lukas.vanillahttpserver.protocol;

/**
 * List of supported HTTP protocols
 */
public enum HttpProtocol {
    HTTP_1("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");

    private final String protocol;

    HttpProtocol(String protocol) {
        this.protocol = protocol;
    }

    static HttpProtocol detect(String token) {
        for (HttpProtocol httpProtocol : HttpProtocol.values()) {
            if (httpProtocol.protocol.equals(token)) {
                return httpProtocol;
            }
        }

        return null;
    }
}
