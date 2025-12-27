package com.example.ledgerly.infra.payment.dto;

public class MasterpassAuthorizeResponse {
    private String authId;
    private String status; // AUTHORIZED / DECLINED
    private String message;

    public MasterpassAuthorizeResponse() {}

    public MasterpassAuthorizeResponse(String authId, String status, String message) {
        this.authId = authId;
        this.status = status;
        this.message = message;
    }

    public String getAuthId() { return authId; }
    public void setAuthId(String authId) { this.authId = authId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
