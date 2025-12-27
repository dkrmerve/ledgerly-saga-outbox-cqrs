package com.example.ledgerly.infra.payment.dto;

public class MasterpassCaptureRequest {
    private String authId;
    private String idempotencyKey;

    public String getAuthId() { return authId; }
    public void setAuthId(String authId) { this.authId = authId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
