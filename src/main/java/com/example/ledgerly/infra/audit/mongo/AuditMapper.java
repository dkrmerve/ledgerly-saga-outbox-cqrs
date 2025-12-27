package com.example.ledgerly.infra.audit.mongo;

import com.example.ledgerly.domain.event.SagaEvent;

import java.util.UUID;

public final class AuditMapper {
    private AuditMapper() {}

    public static AuditTimelineDocument from(UUID correlationId, String idempotencyKey, long orderId, String type, String payloadJson) {
        AuditTimelineDocument d = new AuditTimelineDocument();
        d.id = UUID.randomUUID().toString();
        d.correlationId = correlationId.toString();
        d.idempotencyKey = idempotencyKey;
        d.orderId = orderId;
        d.type = type;
        d.occurredAt = java.time.Instant.now().toString();
        d.payloadJson = payloadJson;
        return d;
    }
}
