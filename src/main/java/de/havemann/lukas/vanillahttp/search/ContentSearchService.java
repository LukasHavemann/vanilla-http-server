package de.havemann.lukas.vanillahttp.search;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;

import java.io.InputStream;
import java.util.Optional;

public interface ContentSearchService {

    Response fetch(String uri);

    interface Response {
        Result getResult();

        Optional<MediaType> getMediaType();

        Optional<InputStream> getInputStream();
    }

    enum Result {
        FOUND(HttpStatusCode.OK),
        NOT_FOUND(HttpStatusCode.NOT_FOUND),
        PERMISSION_DENIED(HttpStatusCode.FORBIDDEN),
        ERROR(HttpStatusCode.INTERNAL_SERVER_ERROR);

        private final HttpStatusCode defaultHttpCode;

        Result(HttpStatusCode defaultHttpCode) {
            this.defaultHttpCode = defaultHttpCode;
        }

        public HttpStatusCode getDefaultHttpCode() {
            return defaultHttpCode;
        }
    }
}
