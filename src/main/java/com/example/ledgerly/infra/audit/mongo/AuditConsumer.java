package com.example.ledgerly.infra.audit.mongo;

import com.example.ledgerly.application.inbox.InboxService;
import com.example.ledgerly.infra.kafka.KafkaHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);
    private static final String CONSUMER = "audit-consumer";

    private final InboxService inbox;
    private final AuditTimelineRepository repo;

    public AuditConsumer(InboxService inbox, AuditTimelineRepository repo) {
        this.inbox = inbox;
        this.repo = repo;
    }

    @KafkaListener(topics = "#{T(com.example.ledgerly.infra.kafka.KafkaTopics).topicAuditEvents()}", containerFactory = "kafkaListenerContainerFactory")
    public void onAudit(ConsumerRecord<String, String> record, Acknowledgment ack) {
        UUID eventId = UUID.fromString(header(record, KafkaHeaders.EVENT_ID));
        if (!inbox.claim(CONSUMER, eventId)) {
            log.info("Duplicate audit event dropped");
            ack.acknowledge();
            return;
        }

        UUID correlationId = UUID.fromString(header(record, KafkaHeaders.CORRELATION_ID));
        String idem = headerOptional(record, KafkaHeaders.IDEMPOTENCY_KEY);
        String orderId = header(record, KafkaHeaders.ORDER_ID);
        String type = headerOptional(record, KafkaHeaders.EVENT_TYPE);

        repo.save(AuditMapper.from(correlationId, idem, Long.parseLong(orderId), type, record.value()));
        ack.acknowledge();
    }

    private String header(ConsumerRecord<String, String> record, String key) {
        var h = record.headers().lastHeader(key);
        if (h == null) throw new IllegalArgumentException("Missing header: " + key);
        return new String(h.value(), StandardCharsets.UTF_8);
    }

    private String headerOptional(ConsumerRecord<String, String> record, String key) {
        var h = record.headers().lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}
