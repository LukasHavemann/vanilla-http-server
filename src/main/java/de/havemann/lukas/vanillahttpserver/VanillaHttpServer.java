package de.havemann.lukas.vanillahttpserver;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


@SpringBootApplication
public class VanillaHttpServer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(VanillaHttpServer.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.CONSOLE)
                .run(args);
    }
}

