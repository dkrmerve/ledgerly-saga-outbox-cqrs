package com.example.ledgerly.domain.order;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_AUTHORIZED,
    PAYMENT_FAILED,
    STOCK_RESERVED,
    STOCK_RESERVE_FAILED,
    CAPTURED,
    COMPLETED,
    CANCELLED
}
