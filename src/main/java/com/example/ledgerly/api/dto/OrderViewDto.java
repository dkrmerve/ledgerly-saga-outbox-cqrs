package com.example.ledgerly.api.dto;

public class OrderViewDto {
    public long orderId;
    public String status;
    public String lastEventType;
    public long version;
    public String updatedAt;
    // add this field to your read model document
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
