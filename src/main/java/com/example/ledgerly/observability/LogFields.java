package com.example.ledgerly.observability;

public final class LogFields {
    private LogFields() {}
    public static final String CORRELATION_ID = "correlationId";
    public static final String IDEMPOTENCY_KEY = "idempotencyKey";
    public static final String ORDER_ID = "orderId";
    public static final String EVENT_ID = "eventId";
    public static final String SAGA_STEP = "sagaStep";
    public static final String KAFKA_TOPIC = "kafkaTopic";
    public static final String KAFKA_PARTITION = "kafkaPartition";
    public static final String KAFKA_OFFSET = "kafkaOffset";
}
