package de.havemann.lukas.vanillahttpserver.protocol;

public enum HttpMethod {
    GET, HEAD;

    public static HttpMethod detect(String method) {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (httpMethod.name().equals(method)) {
                return httpMethod;
            }
        }

        return null;
    }
}
