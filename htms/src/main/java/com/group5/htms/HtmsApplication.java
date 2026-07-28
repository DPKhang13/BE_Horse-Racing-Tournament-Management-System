package com.group5.htms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HtmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HtmsApplication.class, args);
    }

}

