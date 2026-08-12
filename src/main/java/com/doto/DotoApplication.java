package com.doto;

import com.doto.global.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DotoApplication {

    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(DotoApplication.class, args);
    }
}
