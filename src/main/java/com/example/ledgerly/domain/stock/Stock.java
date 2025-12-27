package com.example.ledgerly.domain.stock;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="stocks")
public class Stock {

    @Id
    private String sku;

    @Column(nullable=false)
    private int available;

    @Column(nullable=false)
    private int reserved;

    @Version
    @Column(nullable=false)
    private long version;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    // getters/setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
