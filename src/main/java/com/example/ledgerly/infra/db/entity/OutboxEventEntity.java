package com.example.ledgerly.infra.db.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="outbox_events")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(nullable=false)
    private String topic;

    @Column(name="aggregate_type", nullable=false)
    private String aggregateType;

    @Column(name="aggregate_id", nullable=false)
    private String aggregateId;

    @Column(name="event_type", nullable=false)
    private String eventType;

    @Column(columnDefinition="jsonb", nullable=false)
    private String payload;

    @Column(nullable=false)
    private String status;

    @Column(name="correlation_id", nullable=false)
    private UUID correlationId;

    @Column(name="idempotency_key")
    private String idempotencyKey;

    @Column(name="occurred_at", nullable=false)
    private Instant occurredAt;

    @Column(name="available_at", nullable=false)
    private Instant availableAt;

    @Column(name="published_at")
    private Instant publishedAt;

    @Column(name="publish_attempts", nullable=false)
    private int publishAttempts;

    @Column(name="last_error")
    private String lastError;

    @Column(name="locked_by")
    private String lockedBy;

    @Column(name="lock_until")
    private Instant lockUntil;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public int getPublishAttempts() { return publishAttempts; }
    public void setPublishAttempts(int publishAttempts) { this.publishAttempts = publishAttempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public String getLockedBy() { return lockedBy; }
    public void setLockedBy(String lockedBy) { this.lockedBy = lockedBy; }
    public Instant getLockUntil() { return lockUntil; }
    public void setLockUntil(Instant lockUntil) { this.lockUntil = lockUntil; }
}
