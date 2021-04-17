package de.havemann.lukas.vanillahttpserver.service;

import java.net.Socket;

public interface ClientConnectionHandler {

    boolean offer(Socket clientSocket);
}
