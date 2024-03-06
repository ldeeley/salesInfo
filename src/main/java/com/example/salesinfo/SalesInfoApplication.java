package com.example.salesinfo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SalesInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesInfoApplication.class, args);
    }

}
