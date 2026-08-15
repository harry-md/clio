package com.harry.clio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClioApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClioApplication.class, args);
    }
}
