package com.vkeonline.payment;

import com.vkeonline.base.model.Order;
import com.vkeonline.payment.domain.Customer;
import com.vkeonline.payment.repository.CustomerRepository;
import com.vkeonline.payment.service.OrderManageService;
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
@EnableKafka
@AllArgsConstructor
@Slf4j
public class PaymentApp {

    private final CustomerRepository repository;
    private final OrderManageService orderManageService ;

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);
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
