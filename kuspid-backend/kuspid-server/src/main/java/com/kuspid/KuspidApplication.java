package com.kuspid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KuspidApplication {
    public static void main(String[] args) {
        SpringApplication.run(KuspidApplication.class, args);
    }
}
