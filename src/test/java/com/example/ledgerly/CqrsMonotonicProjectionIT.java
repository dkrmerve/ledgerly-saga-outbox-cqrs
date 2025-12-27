package com.example.ledgerly;

import com.example.ledgerly.infra.kafka.KafkaTopics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CqrsMonotonicProjectionIT extends AbstractIntegrationTest {

    @Autowired KafkaTemplate<String, String> kafkaTemplate;
    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;

    @Test
    void projectionShouldNotGoBack_whenOutOfOrderEventsArrive() throws Exception {
        long orderId = 999999L;

        String eV2 = envelope(orderId, 2, "ORDER_COMPLETED", "COMPLETED");
        String eV1 = envelope(orderId, 1, "ORDER_CREATED", "CREATED");

        kafkaTemplate.send(KafkaTopics.topicDomainEvents(), String.valueOf(orderId), eV2).completable().join();
        kafkaTemplate.send(KafkaTopics.topicDomainEvents(), String.valueOf(orderId), eV1).completable().join();

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    ResponseEntity<JsonNode> view = rest.getForEntity("/api/query/order-views/" + orderId, JsonNode.class);
                    assertThat(view.getStatusCode().is2xxSuccessful()).isTrue();
                    assertThat(view.getBody().get("version").asLong()).isEqualTo(2L);
                    assertThat(view.getBody().get("status").asText()).isEqualTo("COMPLETED");
                });
    }

    private String envelope(long orderId, long version, String type, String status) throws Exception {
        return om.createObjectNode()
                .put("eventId", UUID.randomUUID().toString())
                .put("type", type)
                .put("aggregateId", String.valueOf(orderId))
                .put("version", version)
                .put("occurredAt", Instant.now().toString())
                .put("correlationId", UUID.randomUUID().toString())
                .put("idempotencyKey", UUID.randomUUID().toString())
                .set("payload", om.createObjectNode()
                        .put("orderId", orderId)
                        .put("status", status)
                ).toString();
    }
}
