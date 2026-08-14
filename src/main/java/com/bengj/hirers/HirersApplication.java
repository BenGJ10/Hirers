package com.bengj.hirers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HirersApplication {

    public static void main(String[] args) {
        SpringApplication.run(HirersApplication.class, args);
        System.out.println("Hi, Welcome to Hirers!");
    }
}
