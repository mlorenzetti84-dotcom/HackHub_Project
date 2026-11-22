package com.hackhub.config;

import com.hackhub.domain.test.SimulazioneInterattiva;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("cli")
public class CliSimulationRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        new SimulazioneInterattiva().start();
    }
}
