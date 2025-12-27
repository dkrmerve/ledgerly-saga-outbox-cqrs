package com.example.ledgerly.infra.kafka;

public final class KafkaHeaders {
    private KafkaHeaders() {}
    public static final String CORRELATION_ID = "x-correlation-id";
    public static final String IDEMPOTENCY_KEY = "idempotency-key";
    public static final String EVENT_ID = "event-id";
    public static final String ORDER_ID = "order-id";
    public static final String EVENT_TYPE = "event-type";
}
