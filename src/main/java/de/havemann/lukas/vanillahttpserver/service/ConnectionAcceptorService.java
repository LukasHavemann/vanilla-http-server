package de.havemann.lukas.vanillahttpserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


@Service
public class ConnectionAcceptorService {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionAcceptorService.class);

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("runnint");
    }
}
