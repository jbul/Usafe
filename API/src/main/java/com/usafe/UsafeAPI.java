package com.usafe;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UsafeAPI {

    public static void main(String[] args) {
        SpringApplication.run(UsafeAPI.class);
    }

    @Bean
    public CommandLineRunner run() {
        return (args) -> {

        };
    }
}
