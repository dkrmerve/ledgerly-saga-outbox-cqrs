package com.example.ledgerly.infra.kafka;

import com.example.ledgerly.infra.db.entity.OutboxEventEntity;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OutboxKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OutboxEventEntity e) {
        // key = aggregateId ensures per-order ordering in a single partition (if topic has enough partitions).
        ProducerRecord<String, String> record = new ProducerRecord<>(e.getTopic(), e.getAggregateId(), e.getPayload());

        record.headers().add(KafkaHeaders.CORRELATION_ID, e.getCorrelationId().toString().getBytes(StandardCharsets.UTF_8));
        if (e.getIdempotencyKey() != null) {
            record.headers().add(KafkaHeaders.IDEMPOTENCY_KEY, e.getIdempotencyKey().getBytes(StandardCharsets.UTF_8));
        }
        record.headers().add(KafkaHeaders.EVENT_ID, e.getId().toString().getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.ORDER_ID, e.getAggregateId().getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.EVENT_TYPE, e.getEventType().getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record).completable().join();
    }
}
