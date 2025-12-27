package com.example.ledgerly.domain.event;

import java.util.UUID;

public class SagaEvent {

    public String eventType;
    public long orderId;

    // version for ordering / projection guards
    public long orderVersion;

    // observability / tracing
    public UUID correlationId;
    public String idempotencyKey;

    // optional fields
    public String paymentAuthId;

    public SagaEvent() {}

    public static SagaEvent of(
            String eventType,
            long orderId,
            long orderVersion,
            UUID correlationId,
            String idempotencyKey
    ) {
        SagaEvent e = new SagaEvent();
        e.eventType = eventType;
        e.orderId = orderId;
        e.orderVersion = orderVersion;
        e.correlationId = correlationId;
        e.idempotencyKey = idempotencyKey;
        return e;
    }
}