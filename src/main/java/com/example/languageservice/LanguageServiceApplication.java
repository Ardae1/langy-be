package com.example.languageservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LanguageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LanguageServiceApplication.class, args);
    }
}
