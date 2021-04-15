package de.havemann.lukas.vanillahttpserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ConnectionAcceptorService {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionAcceptorService.class);
    public static final Duration ACCEPTOR_THREAD_TIMEOUT = Duration.ofMillis(1000);

    private final ClientConnectionHandler clientConnectionHandler;

    @Value("${vanilla.server.port}")
    private Integer port;

    @Value("${vanilla.server.host}")
    private String host;

    @Value("${vanilla.server.backlog}")
    private Integer backlog;

    private final AtomicReference<AcceptorThreadState> state = new AtomicReference<>(AcceptorThreadState.NOT_STARTED);
    private ServerSocket acceptingSocket;
    private Thread acceptorThread;

    public ConnectionAcceptorService(ClientConnectionHandler clientConnectionHandler) {
        this.clientConnectionHandler = clientConnectionHandler;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        openSocket();

        LOG.info("start excepting client connections");
        acceptorThread = new Thread(this::acceptClientConnections);
        acceptorThread.setName("acceptThread");
        acceptorThread.setPriority(Thread.MAX_PRIORITY);
        acceptorThread.start();
    }

    private void openSocket() {
        try {
            acceptingSocket = new ServerSocket(port, backlog, InetAddress.getByName(host));
            acceptingSocket.setSoTimeout((int) ACCEPTOR_THREAD_TIMEOUT.toMillis());
            LOG.info("started vanilla http server on {} and port {}", host, port);
        } catch (IOException e) {
            LOG.error("could not open socket on {} and port {}", host, port, e);
        }
    }

    private void acceptClientConnections() {
        state.set(AcceptorThreadState.RUNNING);

        while (state.get() == AcceptorThreadState.RUNNING) {
            Socket clientSocket;
            try {
                clientSocket = acceptingSocket.accept();
                if (!clientConnectionHandler.offer(clientSocket)) {
                    // TODO: 15.04.21 reject connection by to many request

                }
            } catch (SocketTimeoutException e) {
                LOG.debug("clientSocket timeout. free thread to check shutdown flag");
            } catch (IOException e) {
                LOG.error("unexpected error", e);
            }
        }
        state.set(AcceptorThreadState.STOPPED);
    }

    @EventListener
    public void shutdown(ContextClosedEvent stoppedEvent) throws InterruptedException {
        if (!state.compareAndSet(AcceptorThreadState.RUNNING, AcceptorThreadState.STOPPING)) {
            LOG.warn("Thread not started or already stopped. current state {}", state.get());
            return;
        }

        if (acceptorThread != null) {
            acceptorThread.join(ACCEPTOR_THREAD_TIMEOUT.multipliedBy(2).toMillis());
        }

        closeAcceptingSocket();

        LOG.info("Acceptor socket closed");
    }

    private void closeAcceptingSocket() {
        try {
            if (acceptingSocket != null) {
                acceptingSocket.close();
            }
        } catch (IOException e) {
            LOG.error("error during close of accepting socket", e);
        }
    }

    private enum AcceptorThreadState {
        NOT_STARTED, RUNNING, STOPPING, STOPPED
    }
}
