package de.havemann.lukas.vanillahttpserver.protocol;

import java.util.Objects;

/**
 * Error during parsing of http-request. Please refer to {@link #getReason()} for parsing error reason.
 */
public class HttpParsingException extends RuntimeException {

    private final Reason reason;
    private final String errorToken;

    public HttpParsingException(Reason reason) {
        super(buildMessage(reason, null));
        this.reason = Objects.requireNonNull(reason);
        this.errorToken = null;
    }

    public HttpParsingException(Reason reason, String token) {
        super(buildMessage(reason, token));
        this.reason = Objects.requireNonNull(reason);
        this.errorToken = Objects.requireNonNull(token);
    }

    private static String buildMessage(Reason reason, String token) {
        final String message = "parsing error due to " + reason;
        if (token != null) {
            return message + " found token '" + token + "'";
        }

        return message;
    }

    /**
     * Reason why parsing of http-request failed
     *
     * @return reason why parsing of http-request failed
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * @return token, on which the parsing failed
     */
    public String getErrorToken() {
        return errorToken;
    }

    public enum Reason {
        EMPTY_STATUS_LINE,
        UNSUPPORTED_HTTP_METHOD,
        URI_EXPECTED,
        NO_HTTP_PROTOCOL_FOUND,
        UNSUPPORTED_HTTP_PROTOCOL,
        INVALID_HTTP_HEADER_FIELD
    }
}
