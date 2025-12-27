package com.example.ledgerly.infra.mongo;

import com.example.ledgerly.infra.events.DomainEventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderViewProjector {

    private final OrderViewRepository repo;
    private final ObjectMapper om;

    public OrderViewProjector(OrderViewRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    @KafkaListener(
            topics = "#{T(com.example.ledgerly.infra.kafka.KafkaTopics).topicDomainEvents()}",
            groupId = "order-view-projector"
    )
    public void onEvent(String raw) throws Exception {
        DomainEventEnvelope event = om.readValue(raw, DomainEventEnvelope.class);

        Long orderId = Long.valueOf(event.getAggregateId());

        OrderView existing = repo.findById(orderId).orElse(null);

        if (existing != null && event.getVersion() <= existing.getVersion()) {
            return;
        }

        OrderView view = (existing != null) ? existing : new OrderView();
        view.setOrderId(orderId);

        if (event.getPayload() != null && event.getPayload().hasNonNull("status")) {
            view.setStatus(event.getPayload().get("status").asText());
        } else {
            view.setStatus(event.getType());
        }

        view.setVersion(event.getVersion());
        repo.save(view);
    }
}
