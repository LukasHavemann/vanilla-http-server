package de.havemann.lukas.vanillahttpserver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleSmokeTest {

    @Test
    public void smokeTest() {
        VanillaHttpServer.main(null);
    }

    @Test
    public void failingTest() {
        assertThat(1).isEqualTo(3);
    }
}
