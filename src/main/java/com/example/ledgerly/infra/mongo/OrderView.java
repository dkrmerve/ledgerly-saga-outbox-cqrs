package com.example.ledgerly.infra.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("order_views")
public class OrderView {

    @Id
    private Long orderId;

    private String status;

    private long version;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
