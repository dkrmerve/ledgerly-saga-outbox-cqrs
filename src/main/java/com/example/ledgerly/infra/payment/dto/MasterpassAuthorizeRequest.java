package com.example.ledgerly.infra.payment.dto;

public class MasterpassAuthorizeRequest {
    private long orderId;
    private String amount;        // keep string to avoid floating issues
    private String currency;
    private String idempotencyKey;

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
