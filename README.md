# kafka-transaction
Simple Microservice Application with Kafka stream

1. setup database application
2. setup Kafka stream 

### How to run

curl -X POST http://localhost:8080/orders/generate

### Kafka commands

#### list topics
kafka-topics.sh --bootstrap-server localhost:9092 --list
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic orders
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic payment-orders
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic stock-orders

#### soft delete data
kafka-configs.sh --bootstrap-server localhost:9092 --alter --topic orders --add-config retention.ms=1000
kafka-configs.sh --bootstrap-server localhost:9092 --alter --topic stock-orders --add-config retention.ms=1000
kafka-configs.sh --bootstrap-server localhost:9092 --alter --topic payment-orders --add-config retention.ms=1000
kafka-configs.sh --bootstrap-server localhost:9092 --alter --topic orders --add-config retention.ms=604800000
kafka-configs.sh --bootstrap-server localhost:9092 --alter --topic stock-orders --add-config retention.ms=604800000
kafka-configs.sh --bootstrap-server localhost:9092 --alter --topic payment-orders --add-config retention.ms=604800000

#### view kafka messages
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders --from-beginning