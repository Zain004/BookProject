package com.example.versjon2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Versjon2Application {

    public static void main(String[] args) {
        SpringApplication.run(Versjon2Application.class, args);
    }

}
