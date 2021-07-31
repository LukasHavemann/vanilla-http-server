package de.havemann.lukas.vanillahttp.dispatcher;

import java.net.Socket;

/**
 * Receives new established client sockets and processes them.
 */
@SuppressWarnings("UnusedReturnValue")
public interface ClientSocketDispatcher {

  /**
   * process new established client socket
   *
   * @param clientSocket new established client socket
   */
  void dispatch(Socket clientSocket);
}
