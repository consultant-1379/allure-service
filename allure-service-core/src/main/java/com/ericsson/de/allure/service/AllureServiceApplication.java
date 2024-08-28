package com.ericsson.de.allure.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class AllureServiceApplication extends SpringBootServletInitializer {

    public static void main(String... args) {
        new SpringApplication(AllureServiceApplication.class).run(args);
    }
}
