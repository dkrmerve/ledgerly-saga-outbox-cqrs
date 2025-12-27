package com.example.ledgerly.api;

import com.example.ledgerly.application.idempotency.IdempotencyService;
import com.example.ledgerly.application.order.OrderService;
import com.example.ledgerly.domain.money.Money;
import com.example.ledgerly.domain.order.Order;
import com.example.ledgerly.domain.order.OrderItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final IdempotencyService idempotencyService;

    public OrderController(OrderService orderService, IdempotencyService idempotencyService) {
        this.orderService = orderService;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Create an order and start the saga via transactional outbox.
     *
     * Idempotency:
     * - Client must send Idempotency-Key header
     * - If the same key is reused with the same request: we return the stored response
     * - If key is reused with different request: 409
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateOrderRequest req
    ) {
        return idempotencyService.execute(
                idempotencyKey,
                req,
                () -> {
                    Money total = Money.of(new BigDecimal(req.totalAmount), req.totalCurrency);

                    List<OrderItem> items = new ArrayList<>();
                    if (req.items != null) {
                        for (CreateOrderItem i : req.items) {
                            OrderItem oi = new OrderItem();
                            oi.setSku(i.sku);
                            oi.setQuantity(i.quantity);
                            items.add(oi);
                        }
                    }

                    Order order = orderService.createAndStartSaga(
                            req.externalOrderId,
                            idempotencyKey,
                            total,
                            items
                    );

                    return CreateOrderResponse.from(order);
                }
        );
    }

    // ---------- DTOs (keep in controller for demo; you can move to api.dto) ----------

    public static class CreateOrderRequest {
        public String externalOrderId;
        public String totalAmount;     // string to avoid floating issues
        public String totalCurrency;   // "EUR"
        public List<CreateOrderItem> items;
    }

    public static class CreateOrderItem {
        public String sku;
        public int quantity;
    }

    public static class CreateOrderResponse {
        public Long orderId;
        public String externalOrderId;
        public String status;
        public long version;

        public static CreateOrderResponse from(Order o) {
            CreateOrderResponse r = new CreateOrderResponse();
            r.orderId = o.getId();
            r.externalOrderId = o.getExternalOrderId();
            r.status = o.getStatus().name();
            r.version = o.getVersion();
            return r;
        }
    }
}