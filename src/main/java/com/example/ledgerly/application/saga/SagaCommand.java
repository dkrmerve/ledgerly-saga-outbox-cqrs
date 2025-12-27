package com.example.ledgerly.application.saga;

import java.time.Instant;
import java.util.UUID;

/**
 * Minimal saga command envelope for Kafka messaging.
 * This is intentionally simple: enough for orchestration and tracing.
 */
public class SagaCommand {

    private UUID commandId;
    private String type;
    private String aggregateId;
    private Instant createdAt;

    private String correlationId;
    private String idempotencyKey;

    private String payloadJson;

    public UUID getCommandId() { return commandId; }
    public void setCommandId(UUID commandId) { this.commandId = commandId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
}