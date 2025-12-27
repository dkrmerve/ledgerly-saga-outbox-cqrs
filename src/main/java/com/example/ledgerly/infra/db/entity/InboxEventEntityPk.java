package com.example.ledgerly.infra.db.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class InboxEventEntityPk implements Serializable {
    public UUID eventId;
    public String consumer;

    public InboxEventEntityPk() {}

    public InboxEventEntityPk(UUID eventId, String consumer) {
        this.eventId = eventId;
        this.consumer = consumer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InboxEventEntityPk that = (InboxEventEntityPk) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(consumer, that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, consumer);
    }
}
