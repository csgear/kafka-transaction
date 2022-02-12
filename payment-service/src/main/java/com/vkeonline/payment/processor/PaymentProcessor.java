package com.vkeonline.payment.processor;


import com.vkeonline.base.model.Order;
import com.vkeonline.payment.domain.PaymentAggregator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentProcessor {
    private static final String PAYMENT_STREAM = "payment_stream" ;

    private final KafkaTemplate<Long, Order> template;
    private final Random random = new Random();

    private PaymentAggregator aggregatorService(
            Long id, Order order, PaymentAggregator rsv) {
        switch (order.getStatus()) {
            case "CONFIRMED" ->
                    rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
            case "ROLLBACK" -> {
                if (!order.getSource().equals("PAYMENT")) {
                    rsv.setAmountAvailable(rsv.getAmountAvailable() + order.getPrice());
                    rsv.setAmountReserved(rsv.getAmountReserved() - order.getPrice());
                }
            }
            case "NEW" -> {
                if (order.getPrice() <= rsv.getAmountAvailable()) {
                    rsv.setAmountAvailable(rsv.getAmountAvailable()
                            - order.getPrice());
                    rsv.setAmountReserved(rsv.getAmountReserved() + order.getPrice());
                    order.setStatus("ACCEPT");
                } else {
                    order.setStatus("REJECT");
                }
                template.send("payment-orders", order.getId(), order);
            }
        }
        log.info("{}", rsv);
        return rsv;
    }


    @Bean(PAYMENT_STREAM)
    public KStream<Long, Order> paymentStream(StreamsBuilder builder) {
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        JsonSerde<PaymentAggregator> rsvSerde = new JsonSerde<>(PaymentAggregator.class);

        KStream<Long, Order> stream = builder
                .stream("orders", Consumed.with(Serdes.Long(), orderSerde))
                .peek((k, order) -> log.info("New: {}", order));

        KeyValueBytesStoreSupplier customerOrderStoreSupplier =
                Stores.persistentKeyValueStore("customer-orders");

        stream
                .selectKey((k,v) -> v.getCustomerId())
                .groupByKey(Grouped.with(Serdes.Long(), orderSerde))
                .aggregate(
                        () -> new PaymentAggregator(random.nextInt(1000)),
                        this::aggregatorService,
                        Materialized.<Long, PaymentAggregator>as(customerOrderStoreSupplier)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(rsvSerde))
                .toStream()
                .peek((k, trx) -> log.info("Commit: {}", trx));

        return stream ;
    }
}
