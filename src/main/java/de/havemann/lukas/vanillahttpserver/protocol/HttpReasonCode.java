package de.havemann.lukas.vanillahttpserver.protocol;

import java.util.Objects;

public enum HttpReasonCode {

    BAD_REQUEST(400, "Bad Request"),
    FORBIDDEN(403, "Forbidden");

    private final int code;
    private final String description;

    HttpReasonCode(int code, String description) {
        this.code = code;
        this.description = Objects.requireNonNull(description);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
