package com.oriole.wisepen.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableScheduling
@EnableMongoAuditing
@SpringBootApplication
public class ResPermissionApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResPermissionApplication.class, args);
    }
}