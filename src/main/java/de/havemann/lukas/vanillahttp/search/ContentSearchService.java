package de.havemann.lukas.vanillahttp.search;

import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Service for searching and loading content.
 */
public interface ContentSearchService {

    /**
     * Searches for a resource identified by the supplied uri
     *
     * @param uri identifying the syearched resource
     * @return result object with search result
     */
    Response fetch(String uri);

    interface Response {

        /**
         * Result of executed search operation
         */
        Result getResult();

        Optional<MediaType> getMediaType();

        /**
         * Opens stream to found resource
         */
        Optional<InputStream> getInputStream();

        /**
         * Return Hash-Value of found resource. Call to method may lead to load of resource into memory depending on
         * specific implementation
         *
         * @return hash value from found resource
         */
        Optional<byte[]> getHash() throws IOException;

        /**
         * @return last modification date
         */
        Optional<ZonedDateTime> getLastModified();
    }

    enum Result {
        FOUND(HttpStatusCode.OK),
        NOT_FOUND(HttpStatusCode.NOT_FOUND),
        PERMISSION_DENIED(HttpStatusCode.FORBIDDEN),
        ERROR(HttpStatusCode.INTERNAL_SERVER_ERROR);

        // we are running inside a http server so http technology dependent elements in the api are okay.
        private final HttpStatusCode defaultHttpCode;

        Result(HttpStatusCode defaultHttpCode) {
            this.defaultHttpCode = defaultHttpCode;
        }

        public HttpStatusCode getDefaultHttpCode() {
            return defaultHttpCode;
        }
    }
}
