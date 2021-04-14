package de.havemann.lukas.vanillahttpserver.protocol;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.*;

/**
 * Parsed representation of a HTTP request
 */
public class HttpRequest {
    private final HttpMethod httpMethod;
    private final String uri;
    private final HttpProtocol httpProtocol;
    private final String messageBody;
    private final Map<String, String> httpHeaders;

    public HttpRequest(Builder builder) {
        this.httpMethod = Objects.requireNonNull(builder.httpMethod);
        this.uri = Objects.requireNonNull(builder.uri);
        this.httpProtocol = Objects.requireNonNull(builder.httpProtocol);
        this.messageBody = builder.messageBody;
        this.httpHeaders = Collections.unmodifiableMap(builder.httpHeaders);
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getUri() {
        return uri;
    }

    public HttpProtocol getHttpProtocol() {
        return httpProtocol;
    }

    public Optional<String> getMessageBody() {
        return Optional.ofNullable(messageBody);
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public static class Builder {
        private HttpMethod httpMethod;
        private String uri;
        private HttpProtocol httpProtocol;
        private String messageBody;
        private final Map<String, String> httpHeaders = new HashMap<>();

        public void httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
        }

        public void requestUri(String uri) {
            this.uri = uri;
        }

        public void httpProtocol(HttpProtocol httpProtocol) {
            this.httpProtocol = httpProtocol;
        }

        public void addHttpHeader(String key, String value) {
            final String previous = httpHeaders.put(key, value);
            // RFC 7230 requeires folding of same http header fields
            if (previous != null) {
                httpHeaders.put(key, previous + "," + value);
            }
        }

        public void messageBody(String messageBody) {
            this.messageBody = messageBody;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    @Override
    public String toString() {
        // for testing purpose only
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public int hashCode() {
        // for testing purpose only
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        // for testing purpose only
        return EqualsBuilder.reflectionEquals(this, other);
    }
}