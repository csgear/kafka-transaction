package com.vkeonline.payment.redis;

import com.vkeonline.base.model.Order;
import com.vkeonline.payment.redis.domain.Customer;
import com.vkeonline.payment.redis.repository.CustomerRepository;
import com.vkeonline.payment.redis.service.OrderManageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.PostConstruct;
import java.util.Random;

@SpringBootApplication
@AllArgsConstructor
@EnableKafka
@Slf4j
public class PaymentRedisApp {
    private final OrderManageService orderManageService ;
    private final CustomerRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(PaymentRedisApp.class, args);
    }

    @KafkaListener(id = "orders", topics = "orders", groupId = "payment")
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
        Faker faker = new Faker();
        for (int i = 0; i < 100; i++) {
            int count = r.nextInt(1000);
            Customer c = new Customer(null, faker.name().fullName(), count, 0);
            repository.save(c);
        }
    }
}
