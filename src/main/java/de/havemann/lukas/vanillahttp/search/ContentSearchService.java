package de.havemann.lukas.vanillahttp.search;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface ContentSearchService {

    /**
     * Searches for a resource identified by the supplied uri
     *
     * @param uri identifying the searched resource
     * @return result object with search result
     */
    Response fetch(String uri);

    interface Response {

        Result getResult();

        Optional<MediaType> getMediaType();

        Optional<InputStream> getInputStream() throws IOException;

        /**
         * Return Hash-Value of found resource. Call to method may lead to load of resource into memory depending on
         * implementation of search-service
         *
         * @return calculated hash value
         */
        Optional<byte[]> getHash() throws IOException;

        Optional<ZonedDateTime> getLastModified();
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
