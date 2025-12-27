package com.example.ledgerly.infra.payment.dto;

public class MasterpassCaptureResponse {
    private String status; // CAPTURED / FAILED
    private String message;

    public MasterpassCaptureResponse() {}

    public MasterpassCaptureResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
