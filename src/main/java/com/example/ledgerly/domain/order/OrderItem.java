package com.example.ledgerly.domain.order;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="order_id")
    private Order order;

    @Column(nullable=false)
    private String sku;

    @Column(nullable=false)
    private int quantity;

    @Column(name="unit_amount", nullable=false)
    private BigDecimal unitAmount;

    @Column(nullable=false, length=3)
    private String currency;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    public void setUnitAmount(com.example.ledgerly.domain.money.Money money) {
        this.unitAmount = money.amount();
        this.currency = money.currencyCode();
    }

    // getters/setters
    public void setOrder(Order order) { this.order = order; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public com.example.ledgerly.domain.money.Money getUnitAmount() {
        return com.example.ledgerly.domain.money.Money.of(unitAmount, currency);
    }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
