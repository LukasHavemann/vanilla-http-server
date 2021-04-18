package de.havemann.lukas.vanillahttp.protocol.request;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeader;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpMethod;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;

/**
 * Parsed representation of a HTTP request
 */
public class HttpRequest {
    private final HttpMethod httpMethod;
    private final String uri;
    private final HttpProtocol httpProtocol;
    private final String messageBody;
    private final HttpHeader httpHeader;

    public HttpRequest(Builder builder) {
        this.httpMethod = Objects.requireNonNull(builder.httpMethod);
        this.uri = Objects.requireNonNull(builder.uri);
        this.httpProtocol = Objects.requireNonNull(builder.httpProtocol);
        this.messageBody = builder.messageBody;
        this.httpHeader = new HttpHeader(Collections.unmodifiableMap(builder.httpHeader));
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

    public HttpHeader getHeader() {
        return httpHeader;
    }

    @Override
    public String toString() {
        // for testing purpose only
        return ToStringBuilder.reflectionToString(this);
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

    @SuppressWarnings("UnusedReturnValue")
    protected static class Builder {
        private final Map<String, String> httpHeader = new HashMap<>();
        private HttpMethod httpMethod;
        private String uri;
        private HttpProtocol httpProtocol;
        private String messageBody;

        public Builder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder httpProtocol(HttpProtocol httpProtocol) {
            this.httpProtocol = httpProtocol;
            return this;
        }

        public Builder addHttpHeader(String key, String value) {
            final String previous = httpHeader.put(key, value);
            // RFC 7230 requires folding of same http header fields
            if (previous != null) {
                httpHeader.put(key, previous + "," + value);
            }
            return this;
        }

        public Builder messageBody(String messageBody) {
            this.messageBody = messageBody;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}
