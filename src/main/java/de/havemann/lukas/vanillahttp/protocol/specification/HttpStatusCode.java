package de.havemann.lukas.vanillahttp.protocol.specification;

import java.util.Objects;

/**
 * list of used http status code. For full list with explanation {@see https://en.wikipedia.org/wiki/List_of_HTTP_status_codes}
 */
public enum HttpStatusCode implements ProtocolRepresentation {

    // 2xx successful operation
    OK(200, "OK"),

    // 3xx redirection,
    NOT_MODIFIED(304, "Not Modified"),

    // 4xx client error
    BAD_REQUEST(400, "Bad Request"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    REQUEST_TIMEOUT(408, "Request Timeout"),

    // 5xx server error
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int code;
    private final String description;

    HttpStatusCode(int code, String description) {
        this.code = code;
        this.description = Objects.requireNonNull(description);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getRepresentation() {
        return code + " " + description;
    }
}
