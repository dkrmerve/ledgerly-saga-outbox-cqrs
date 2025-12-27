package com.example.ledgerly.infra.db.entity;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="idempotency_requests")
public class IdempotencyEntity {
    @Id
    @Column(name="idempotency_key")
    private String idempotencyKey;

    @Column(name="request_hash", nullable=false)
    private String requestHash;

    @Column(name="response_code", nullable=false)
    private int responseCode;

    @Column(name="response_body", columnDefinition="jsonb", nullable=false)
    private String responseBody;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    // getters/setters
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }
    public int getResponseCode() { return responseCode; }
    public void setResponseCode(int responseCode) { this.responseCode = responseCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
