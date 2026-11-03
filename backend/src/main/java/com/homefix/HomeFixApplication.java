package com.homefix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HomeFixApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeFixApplication.class, args);
    }
}
