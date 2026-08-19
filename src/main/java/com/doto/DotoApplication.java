package com.doto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DotoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DotoApplication.class, args);
    }
}
