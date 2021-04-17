package de.havemann.lukas.vanillahttp.protocol.response;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpHeaderField;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HttpResponseHeader {

    private final HttpProtocol protocol;
    private final HttpStatusCode statusCode;
    private final List<Pair<HttpHeaderField, String>> headerFields;

    public HttpResponseHeader(Builder builder) {
        this.protocol = Objects.requireNonNull(builder.protocol);
        this.statusCode = Objects.requireNonNull(builder.statusCode);
        this.headerFields = Collections.unmodifiableList(builder.headerFields);
    }

    public HttpProtocol getProtocol() {
        return protocol;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public List<Pair<HttpHeaderField, String>> getHeaderFields() {
        return headerFields;
    }

    public static class Builder {
        private final List<Pair<HttpHeaderField, String>> headerFields = new ArrayList<>();
        private HttpProtocol protocol;
        private HttpStatusCode statusCode;

        public Builder protocol(HttpProtocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder statusCode(HttpStatusCode statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder add(HttpHeaderField headerField, String value) {
            headerFields.add(Pair.of(headerField, value));
            return this;
        }

        public Builder contentType(MediaType mediatype) {
            headerFields.add(Pair.of(HttpHeaderField.CONTENT_TYPE, mediatype.getRepresentation()));
            return this;
        }

        public HttpResponseHeader build() {
            return new HttpResponseHeader(this);
        }
    }
}
