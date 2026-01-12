package com.musinsa.pointsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PointSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointSystemApplication.class, args);
    }
}
