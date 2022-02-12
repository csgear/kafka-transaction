package com.vkeonline.stock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
@Slf4j
@AllArgsConstructor
public class StockApp {

    public static void main(String[] args) {
        SpringApplication.run(StockApp.class, args);
    }

}
