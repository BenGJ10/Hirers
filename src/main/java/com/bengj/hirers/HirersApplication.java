package com.bengj.hirers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableCaching
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImplementation")
public class HirersApplication {

    public static void main(String[] args) {
        SpringApplication.run(HirersApplication.class, args);
        System.out.println("Hi, Welcome to Hirers!");
    }
}
