package com.example.ledgerly.api.dto;

public class CreateOrderResponse {
    public long orderId;
    public String status;

    public CreateOrderResponse(long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }
}
