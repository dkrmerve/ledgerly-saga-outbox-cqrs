package com.example.ledgerly.infra.events;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public class DomainEventEnvelope {

    private String eventId;
    private String type;
    private String aggregateId;
    private long version;
    private Instant occurredAt;

    private String correlationId;
    private String idempotencyKey;

    private JsonNode payload;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }
}