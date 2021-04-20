package de.havemann.lukas.vanillahttp;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


/**
 * Simple but full functional implementation of a http-server without a http framework in vanilla java.
 */
@SpringBootApplication
public class VanillaHttpServer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(VanillaHttpServer.class)
                // spring web disabled, so that all http request are handled by vanilla-http-server.
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

