package com.oriole.wisepen.docs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DocsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocsApplication.class, args);
    }
}
