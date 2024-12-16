package com.ycx.backend.http.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Main {

    @GetMapping("/http-demo/ping")
    public String ping() {
        return "pong";
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}