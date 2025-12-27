package com.example.ledgerly.domain.order;

import com.example.ledgerly.domain.money.Money;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_order_id", nullable = false)
    private String externalOrderId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "total_currency", nullable = false, length = 3)
    private String totalCurrency;

    @Column(name = "payment_auth_id")
    private String paymentAuthId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<OrderItem> items = new ArrayList<>();

    /* =========================
       Aggregate behavior
       ========================= */

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    /** Call once when order is first created */
    public void touchNew() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void transitionTo(OrderStatus next) {
        OrderStateMachine.assertTransition(this.status, next);
        this.status = next;
        this.updatedAt = Instant.now();
    }

    /* =========================
       Saga-related methods
       ========================= */

    // Called after successful payment authorization
    public void markPaymentAuthorized(String authId) {
        if (authId == null || authId.trim().isEmpty()) {
            throw new IllegalArgumentException("authId must not be empty");
        }
        this.paymentAuthId = authId;
        transitionTo(OrderStatus.PAYMENT_AUTHORIZED);
    }

    // Called after successful capture
    public void markCaptured() {
        transitionTo(OrderStatus.CAPTURED);
    }

    // Called when payment authorization fails
    public void markPaymentFailed(String reason) {
        this.failureReason = reason;
        transitionTo(OrderStatus.PAYMENT_FAILED);
    }

    // Called when stock reservation fails
    public void markStockReserveFailed(String reason) {
        this.failureReason = reason;
        transitionTo(OrderStatus.STOCK_RESERVE_FAILED);
    }

    // Called when the whole saga completes
    public void markCompleted() {
        transitionTo(OrderStatus.COMPLETED);
    }

    // Called when the saga is cancelled (compensation path)
    public void markCancelled(String reason) {
        this.failureReason = reason;
        transitionTo(OrderStatus.CANCELLED);
    }


    /* =========================
       Money handling
       ========================= */

    public void setTotal(Money money) {
        this.totalAmount = money.amount();
        this.totalCurrency = money.currencyCode();
    }

    /* =========================
       Getters
       ========================= */

    public Long getId() {
        return id;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return totalCurrency;
    }

    public String getPaymentAuthId() {
        return paymentAuthId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void initializeAsPendingPayment() {
        this.status = OrderStatus.PENDING_PAYMENT;
        this.updatedAt = Instant.now();
    }
}
