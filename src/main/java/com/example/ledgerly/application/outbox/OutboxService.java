package com.example.ledgerly.application.outbox;

import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.infra.db.entity.OutboxEventEntity;
import com.example.ledgerly.infra.db.repository.OutboxRepository;
import com.example.ledgerly.infra.kafka.KafkaTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper om = new ObjectMapper();

    public OutboxService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public void enqueueSagaCommand(long orderId, String idempotencyKey, UUID correlationId, SagaEvent event) {
        enqueue(KafkaTopics.topicSagaCommands(), orderId, idempotencyKey, correlationId, event.eventType, event);
    }

    @Transactional
    public void enqueueDomainEvent(long orderId, String idempotencyKey, UUID correlationId, SagaEvent event) {
        enqueue(KafkaTopics.topicDomainEvents(), orderId, idempotencyKey, correlationId, event.eventType, event);
    }

    @Transactional
    public void enqueueAudit(UUID correlationId, String idempotencyKey, long orderId, String eventType, Object payload) {
        enqueue(KafkaTopics.topicAuditEvents(), orderId, idempotencyKey, correlationId, eventType, payload);
    }

    private void enqueue(String topic, long orderId, String idempotencyKey, UUID correlationId, String eventType, Object payload) {
        try {
            OutboxEventEntity e = new OutboxEventEntity();
            e.setId(UUID.randomUUID());
            e.setTopic(topic);
            e.setAggregateType("ORDER");
            e.setAggregateId(String.valueOf(orderId));
            e.setEventType(eventType);
            e.setPayload(om.writeValueAsString(payload));
            e.setStatus("NEW");
            e.setCorrelationId(correlationId);
            e.setIdempotencyKey(idempotencyKey);
            e.setOccurredAt(Instant.now());
            e.setAvailableAt(Instant.now());
            e.setPublishAttempts(0);
            outboxRepository.save(e);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
