package de.havemann.lukas.vanillahttpserver.service;

import de.havemann.lukas.vanillahttpserver.protocol.request.HttpRequest;
import de.havemann.lukas.vanillahttpserver.protocol.request.HttpRequestBuffer;
import de.havemann.lukas.vanillahttpserver.protocol.response.HttpResponseHeader;
import de.havemann.lukas.vanillahttpserver.protocol.response.HttpResponseWriter;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpProtocol;
import de.havemann.lukas.vanillahttpserver.protocol.specification.HttpStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Service
public class ClientConnectionWorkerPool implements ClientConnectionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConnectionWorkerPool.class);

    @Autowired
    private FileService fileService;

    public boolean offer(Socket clientSocket) {
        new Thread(() -> {

            InputStream inputStream = null;
            OutputStream outputstream;
            try {
                clientSocket.setSoTimeout(10000);
                inputStream = clientSocket.getInputStream();
                outputstream = clientSocket.getOutputStream();

                new ConnectionHandler(inputStream, outputstream, fileService)
                        .readRequest()
                        .dispatchRequestTo()
                        .writeResponse();

            } catch (Exception e) {
                LOG.error("error during handling of client", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LOG.error("error during inputstream close", e);
                    }
                }
                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        LOG.error("error during clientSocket close", e);
                    }
                }
            }
        }).start();

        return true;
    }
}
