package com.ericsson.de.allure.service.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class AllureServiceConfigServerApplication {

    public static void main(String[] args){
        SpringApplication.run(AllureServiceConfigServerApplication.class, args);
    }
}
