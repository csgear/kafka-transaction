package com.vkeonline.stock;

import com.vkeonline.base.model.Order;
import com.vkeonline.stock.domain.Product;
import com.vkeonline.stock.repository.ProductRepository;
import com.vkeonline.stock.service.OrderManageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.PostConstruct;
import java.util.Random;

@SpringBootApplication
@EnableKafka
@Slf4j
@AllArgsConstructor
public class StockApp {
    private final OrderManageService orderManageService;
    private final ProductRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(StockApp.class, args);
    }

    @KafkaListener(id = "orders", topics = "orders", groupId = "stock")
    public void onEvent(Order o) {
        log.info("Received: {}" , o);
        if (o.getStatus().equals("NEW"))
            orderManageService.reserve(o);
        else
            orderManageService.confirm(o);
    }

    @PostConstruct
    public void generateData() {
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int count = r.nextInt(100);
            Product p = new Product((long) i, "Product" + i, count, 0);
            repository.save(p);
        }
    }
}
