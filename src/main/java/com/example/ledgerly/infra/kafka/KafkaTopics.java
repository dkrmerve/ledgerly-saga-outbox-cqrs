// src/main/java/com/example/ledgerly/infra/kafka/KafkaTopics.java
package com.example.ledgerly.infra.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopics {

    private static String sagaCommands;
    private static String domainEvents;
    private static String auditEvents;

    public KafkaTopics(
            @Value("${ledgerly.kafka.topics.sagaCommands}") String sagaCommands,
            @Value("${ledgerly.kafka.topics.domainEvents}") String domainEvents,
            @Value("${ledgerly.kafka.topics.auditEvents}") String auditEvents
    ) {
        KafkaTopics.sagaCommands = sagaCommands;
        KafkaTopics.domainEvents = domainEvents;
        KafkaTopics.auditEvents = auditEvents;
    }

    public static String topicSagaCommands() { return sagaCommands; }
    public static String topicDomainEvents() { return domainEvents; }
    public static String topicAuditEvents() { return auditEvents; }
}
