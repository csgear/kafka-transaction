package com.vkeonline.order.service;

import com.vkeonline.base.model.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderGeneratorService {
    private static final Random RAND = new Random();
    private final AtomicLong id = new AtomicLong();
    private final Executor executor;
    private final KafkaTemplate<Long, Order> template;

    public OrderGeneratorService(@Qualifier("taskExecutor") Executor executor,
                                 KafkaTemplate<Long, Order> template) {
        this.executor = executor;
        this.template = template;
    }

    @Async
    public void generate() {
        for (int i = 0; i < 5; i++) {
            int x = RAND.nextInt(5) + 1;
            Order o = new Order(id.incrementAndGet(),
                    RAND.nextLong(100) + 1,
                    RAND.nextLong(100) + 1, "NEW");
            o.setPrice(100 * x);
            o.setProductCount(x);
            template.send("orders", o.getId(), o);
        }
    }
}
