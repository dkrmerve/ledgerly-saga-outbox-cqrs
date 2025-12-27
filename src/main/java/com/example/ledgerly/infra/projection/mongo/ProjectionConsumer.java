package com.example.ledgerly.infra.projection.mongo;

import com.example.ledgerly.application.inbox.InboxService;
import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.infra.kafka.KafkaHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class ProjectionConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProjectionConsumer.class);
    private static final String CONSUMER = "projection-consumer";

    private final InboxService inbox;
    private final OrderViewRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    public ProjectionConsumer(InboxService inbox, OrderViewRepository repo) {
        this.inbox = inbox;
        this.repo = repo;
    }

    @KafkaListener(topics = "#{T(com.example.ledgerly.infra.kafka.KafkaTopics).topicDomainEvents()}", containerFactory = "kafkaListenerContainerFactory")
    public void onDomainEvent(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
        UUID eventId = UUID.fromString(header(record, KafkaHeaders.EVENT_ID));
        if (!inbox.claim(CONSUMER, eventId)) {
            log.info("Duplicate domain event dropped");
            ack.acknowledge();
            return;
        }

        SagaEvent e = om.readValue(record.value(), SagaEvent.class);

        OrderViewDocument doc = repo.findById(String.valueOf(e.orderId)).orElseGet(() -> {
            OrderViewDocument d = new OrderViewDocument();
            d.setId(String.valueOf(e.orderId));
            d.setVersion(0);
            return d;
        });

        // Version guard: never regress.
        long prev = doc.getVersion();
        ProjectionMapper.apply(doc, e);
        if (doc.getVersion() < prev) doc.setVersion(prev);

        repo.save(doc);
        ack.acknowledge();
    }

    private String header(ConsumerRecord<String, String> record, String key) {
        var h = record.headers().lastHeader(key);
        if (h == null) throw new IllegalArgumentException("Missing header: " + key);
        return new String(h.value(), StandardCharsets.UTF_8);
    }
}
