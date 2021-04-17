package de.havemann.lukas.vanillahttp.dispatcher;

import java.net.Socket;

public interface ClientConnectionDispatcher {

    boolean dispatch(Socket clientSocket);
}
