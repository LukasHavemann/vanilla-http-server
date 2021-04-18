package de.havemann.lukas.vanillahttp.dispatcher;

import java.net.Socket;

/**
 * Receives and processes new established client sockets and processes them.
 */
@SuppressWarnings("UnusedReturnValue")
public interface ClientSocketDispatcher {

    /**
     * process new established client socket
     *
     * @param clientSocket new established client socket
     * @return true, if dispatching was successful
     */
    boolean dispatch(Socket clientSocket);
}
