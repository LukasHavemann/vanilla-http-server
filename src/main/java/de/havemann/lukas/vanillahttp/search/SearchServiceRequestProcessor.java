package de.havemann.lukas.vanillahttp.search;

import de.havemann.lukas.vanillahttp.dispatcher.ClientConnectionDispatcher;
import de.havemann.lukas.vanillahttp.dispatcher.ClientRequestProcessor;
import de.havemann.lukas.vanillahttp.protocol.request.HttpRequest;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponseHeader;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponseWriter;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpMethod;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@Scope("prototype")
public class SearchServiceRequestProcessor implements ClientRequestProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConnectionDispatcher.class);

    private final HttpResponseWriter httpResponseWriter;
    @Autowired
    private ContentSearchService contentSearchService;

    public SearchServiceRequestProcessor(HttpResponseWriter responseWriter) {
        this.httpResponseWriter = Objects.requireNonNull(responseWriter);
    }

    public void processRequest(HttpRequest request) throws IOException {
        if (request == null) {
            httpResponseWriter.header(new HttpResponseHeader.Builder()
                    .protocol(HttpProtocol.HTTP_1_1)
                    .statusCode(HttpStatusCode.BAD_REQUEST))
                    .finish();
            return;
        }

        final ContentSearchService.Response response = contentSearchService.fetch(request.getUri());

        if (response.getResult() != ContentSearchService.Result.FOUND) {
            httpResponseWriter.header(new HttpResponseHeader.Builder()
                    .protocol(HttpProtocol.HTTP_1_1)
                    .statusCode(response.getResult().getDefaultHttpCode()))
                    .finish();
            return;
        }

        handleFoundSearchResult(request, response);
    }

    private void handleFoundSearchResult(HttpRequest request, ContentSearchService.Response response) throws IOException {
        if (request.getHttpMethod() == HttpMethod.HEAD) {
            httpResponseWriter.header(new HttpResponseHeader.Builder()
                    .protocol(HttpProtocol.HTTP_1_1)
                    .statusCode(response.getResult().getDefaultHttpCode())
                    .contentType(response.getMediaType().orElse(MediaType.UNKNOWN)))
                    .finish();
            return;
        }

        if (request.getHttpMethod() == HttpMethod.GET) {
            httpResponseWriter.header(new HttpResponseHeader.Builder()
                    .protocol(HttpProtocol.HTTP_1_1)
                    .statusCode(response.getResult().getDefaultHttpCode())
                    .contentType(response.getMediaType().orElse(MediaType.UNKNOWN)))
                    .renderChunked(response.getInputStream().orElseThrow(() -> new IllegalStateException(response.toString())))
                    .finish();
            return;
        }

        // error case. if we reach here a programming error occurred
        LOG.error("request not handled {}", request, new IllegalStateException("unhandled request"));

        httpResponseWriter.header(new HttpResponseHeader.Builder()
                .protocol(HttpProtocol.HTTP_1_1)
                .statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR))
                .finish();
    }
}
