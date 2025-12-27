package com.example.ledgerly.domain.event;

public final class EventTypes {
    private EventTypes() {}

    public static final String PAYMENT_AUTH_REQUESTED = "PAYMENT_AUTH_REQUESTED";
    public static final String PAYMENT_AUTHORIZED = "PAYMENT_AUTHORIZED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";

    public static final String STOCK_RESERVE_REQUESTED = "STOCK_RESERVE_REQUESTED";
    public static final String STOCK_RESERVED = "STOCK_RESERVED";
    public static final String STOCK_RESERVE_FAILED = "STOCK_RESERVE_FAILED";

    public static final String PAYMENT_CAPTURE_REQUESTED = "PAYMENT_CAPTURE_REQUESTED";
    public static final String PAYMENT_VOID_REQUESTED = "PAYMENT_VOID_REQUESTED";

    public static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
}
