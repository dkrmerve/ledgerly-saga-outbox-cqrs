package com.example.ledgerly.infra.db.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="inbox_events")
@IdClass(InboxEventEntityPk.class)
public class InboxEventEntity {

    @Id
    @Column(name="event_id", nullable=false)
    private UUID eventId;

    @Id
    @Column(nullable=false)
    private String consumer;

    @Column(name="processed_at", nullable=false)
    private Instant processedAt;

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getConsumer() { return consumer; }
    public void setConsumer(String consumer) { this.consumer = consumer; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
