package com.vkeonline.stock.processor;

import com.vkeonline.base.model.Order;
import com.vkeonline.stock.domain.StockAggregator;
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
public class StockProcessor {
    private static final String STOCK_STREAM = "stock_stream" ;
    private static final Random random = new Random() ;
    private final KafkaTemplate<Long, Order> template;

    @Bean(STOCK_STREAM)
    public KStream<Long, Order> stockStream(StreamsBuilder builder) {
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);
        JsonSerde<StockAggregator> rsvSerde = new JsonSerde<>(StockAggregator.class);
        KStream<Long, Order> stream = builder
                .stream("orders", Consumed.with(Serdes.Long(), orderSerde))
                .peek((k, order) -> log.info("New: {}", order));

        KeyValueBytesStoreSupplier stockOrderStoreSupplier =
                Stores.persistentKeyValueStore("stock-orders");

        Aggregator<Long, Order, StockAggregator> aggrSrv = (id, order, rsv) -> {
            switch (order.getStatus()) {
                case "CONFIRMED" -> rsv.setItemsReserved(rsv.getItemsReserved() - order.getProductCount());
                case "ROLLBACK" -> {
                    if (!order.getSource().equals("STOCK")) {
                        rsv.setItemsAvailable(rsv.getItemsAvailable() + order.getProductCount());
                        rsv.setItemsReserved(rsv.getItemsReserved() - order.getProductCount());
                    }
                }
                case "NEW" -> {
                    if (order.getProductCount() <= rsv.getItemsAvailable()) {
                        rsv.setItemsAvailable(rsv.getItemsAvailable() - order.getProductCount());
                        rsv.setItemsReserved(rsv.getItemsReserved() + order.getProductCount());
                        order.setStatus("ACCEPT");
                    } else {
                        order.setStatus("REJECT");
                    }
                    template.send("stock-orders", order.getId(), order)
                            .addCallback(result -> log.info("Sent: {}",
                                            result != null ? result.getProducerRecord().value() : null),
                                    ex -> {});
                }
            }
            log.info("{}", rsv);
            return rsv;
        };

        stream.selectKey((k, v) -> v.getProductId())
                .groupByKey(Grouped.with(Serdes.Long(), orderSerde))
                .aggregate(() -> new StockAggregator(random.nextInt(100)), aggrSrv,
                        Materialized.<Long, StockAggregator>as(stockOrderStoreSupplier)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(rsvSerde))
                .toStream()
                .peek((k, trx) -> log.info("Commit: {}", trx));

        return stream;
    }
}
