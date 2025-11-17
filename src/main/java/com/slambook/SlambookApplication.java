package com.slambook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class SlambookApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlambookApplication.class, args);
    }

}