package com.example.ledgerly.infra.projection.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("order_view")
public class OrderViewDocument {
    @Id
    private String id; // orderId as string

    private long orderId;
    private String status;
    private String lastEventType;
    private long version;
    private String updatedAt;

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastEventType() { return lastEventType; }
    public void setLastEventType(String lastEventType) { this.lastEventType = lastEventType; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
