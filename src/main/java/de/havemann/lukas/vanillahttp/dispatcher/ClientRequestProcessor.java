package de.havemann.lukas.vanillahttp.dispatcher;

import de.havemann.lukas.vanillahttp.protocol.request.HttpRequest;
import de.havemann.lukas.vanillahttp.protocol.response.HttpResponse;
import java.io.IOException;

/**
 * Accepts and processes incoming {@link HttpRequest}
 */
public interface ClientRequestProcessor {

  /**
   * New HTTP request received
   *
   * @param request  request to be processed
   * @param response builder for creating response to request
   */
  void processRequest(HttpRequest request, HttpResponse.Builder response) throws IOException;
}
