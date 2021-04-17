package de.havemann.lukas.vanillahttp.dispatcher;

import de.havemann.lukas.vanillahttp.protocol.request.HttpRequest;

import java.io.IOException;

public interface ClientRequestProcessor {
    void processRequest(HttpRequest request) throws IOException;
}
