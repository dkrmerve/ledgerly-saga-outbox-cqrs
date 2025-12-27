package com.example.ledgerly.infra.audit.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("audit_timeline")
public class AuditTimelineDocument {
    @Id
    public String id;

    public String correlationId;
    public String idempotencyKey;
    public long orderId;
    public String type;
    public String occurredAt;
    public String payloadJson;
}
