package de.havemann.lukas.vanillahttpserver.protocol.request;

import java.util.Objects;

/**
 * Error during parsing of http-request. Please refer to {@link #getReason()} for parsing error reason.
 */
public class HttpRequestParsingException extends RuntimeException {

    private final Reason reason;
    private final String errorToken;

    private HttpRequestParsingException(Reason reason, String token) {
        super(buildMessage(reason, token));
        this.reason = Objects.requireNonNull(reason);
        this.errorToken = token;
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
     * @return token, on which the parsing process failed
     */
    public String getErrorToken() {
        return errorToken;
    }

    /**
     * Identifies the different parsing errors
     */
    public enum Reason {
        EMPTY_STATUS_LINE,
        UNSUPPORTED_HTTP_METHOD,
        URI_EXPECTED,
        HTTP_PROTOCOL_EXPECTED,
        UNSUPPORTED_HTTP_PROTOCOL,
        INVALID_HTTP_HEADER_FIELD;

        public HttpRequestParsingException toException(String token) {
            return new HttpRequestParsingException(this, Objects.requireNonNull(token));
        }

        public HttpRequestParsingException toException() {
            return new HttpRequestParsingException(this, null);
        }
    }
}
