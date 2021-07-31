package de.havemann.lukas.vanillahttp.search;

import de.havemann.lukas.vanillahttp.dispatcher.ClientRequestProcessor;
import de.havemann.lukas.vanillahttp.dispatcher.ClientSocketDispatcher;
import de.havemann.lukas.vanillahttp.protocol.request.HttpRequest;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponse;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpMethod;
import de.havemann.lukas.vanillahttp.protocol.specification.HttpStatusCode;
import de.havemann.lukas.vanillahttp.protocol.specification.MediaType;
import de.havemann.lukas.vanillahttp.protocol.specification.etag.ETag;
import de.havemann.lukas.vanillahttp.protocol.specification.etag.ETagEvaluator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Contains the main business logic of the vanilla-http-server. Binds together all the different
 * components by excepting {@link HttpRequest}, calling the provided {@link ContentSearchService}
 * instance to find the request resource, evaluating the content search result and generating the
 * HTTP response.
 */
@Component
@Scope("prototype")
public class SearchServiceRequestProcessor implements ClientRequestProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ClientSocketDispatcher.class);

  private final ContentSearchService contentSearchService;

  public SearchServiceRequestProcessor(@Autowired ContentSearchService contentSearchService) {
    this.contentSearchService = contentSearchService;
  }

  public void processRequest(HttpRequest request, HttpResponse.Builder response)
      throws IOException {
    final ContentSearchService.Response searchResponse = contentSearchService
        .fetch(request.getUri());

    if (searchResponse.getResult() != ContentSearchService.Result.FOUND) {
      final HttpStatusCode defaultHttpCode = searchResponse.getResult().getDefaultHttpCode();
      response.statusCode(defaultHttpCode)
          .contentType(MediaType.ASCII_TEXT)
          .payloadRenderer(() -> new ByteArrayInputStream(
              defaultHttpCode.getRepresentation().getBytes(StandardCharsets.UTF_8)));
      return;
    }

    handleFoundSearchResult(request, searchResponse, response);
  }

  private void handleFoundSearchResult(HttpRequest request,
      ContentSearchService.Response searchResponse, HttpResponse.Builder builder)
      throws IOException {
    final boolean shouldContentBeSend = prepareHttpHeader(request, searchResponse, builder);

    if (request.getHttpMethod() == HttpMethod.HEAD) {
      return;
    }

    if (request.getHttpMethod() == HttpMethod.GET) {
      if (shouldContentBeSend) {
        builder.payloadRenderer(() -> searchResponse.getInputStream()
            .orElseThrow(() -> new IllegalStateException(searchResponse.toString())));
      }
      return;
    }

    // error case. if we reach here a programming error occurred
    LOG.error("request not handled {}", request, new IllegalStateException("unhandled request"));

    builder.statusCode(HttpStatusCode.INTERNAL_SERVER_ERROR);
  }

  private boolean prepareHttpHeader(HttpRequest request,
      ContentSearchService.Response searchResponse, HttpResponse.Builder builder)
      throws IOException {
    final Optional<ZonedDateTime> lastModified = searchResponse.getLastModified();
    final Optional<ETag> eTag = searchResponse.getHash()
        .map(hash -> new ETag(hash, ETag.Kind.STRONG));

    builder.statusCode(searchResponse.getResult().getDefaultHttpCode());
    builder.contentType(searchResponse.getMediaType().orElse(MediaType.UNKNOWN));
    lastModified.ifPresent(builder::lastModified);
    eTag.ifPresent(builder::eTag);

    boolean shouldContentBeSend = new ETagEvaluator(request.getHeader())
        .shouldContentBeSend(eTag.orElse(null), lastModified.orElse(null));

    if (!shouldContentBeSend) {
      builder.statusCode(HttpStatusCode.NOT_MODIFIED);
    }

    return shouldContentBeSend;
  }
}
