package com.example.ledgerly.application.saga.steps;

import com.example.ledgerly.application.outbox.OutboxService;
import com.example.ledgerly.domain.event.EventTypes;
import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.domain.stock.Stock;
import com.example.ledgerly.infra.db.repository.OrderRepository;
import com.example.ledgerly.infra.db.repository.StockRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockReserveStep {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final OutboxService outboxService;

    public StockReserveStep(OrderRepository orderRepository,
                            StockRepository stockRepository,
                            OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
        this.outboxService = outboxService;
    }

    public void handle(SagaEvent event) {
        // optimistic retry loop: keep small for demo
        int attempts = 0;
        while (true) {
            try {
                doReserve(event);
                return;
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= 3) {
                    throw e;
                }
            }
        }
    }

    @Transactional
    public void doReserve(SagaEvent event) {
        var order = orderRepository.findById(event.orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.orderId));

        // Reserve stock per item (simplified). You likely already have a StockEntity per SKU.
        // Here we only demonstrate the pattern: read -> check -> update with @Version.
        order.getItems().forEach(item -> {
            Stock stock = stockRepository.findBySku(item.getSku())
                    .orElseThrow(() -> new IllegalArgumentException("Stock not found sku=" + item.getSku()));

            if (stock.getAvailable() < item.getQuantity()) {
                // fail: enqueue void and cancellation
                outboxService.enqueueSagaCommand(order.getId(), event.idempotencyKey, event.correlationId,
                        SagaEvent.of(EventTypes.PAYMENT_VOID_REQUESTED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

                outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                        SagaEvent.of(EventTypes.STOCK_RESERVE_FAILED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

                outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                        SagaEvent.of(EventTypes.ORDER_CANCELLED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

                throw new IllegalStateException("Insufficient stock sku=" + item.getSku());
            }

            stock.setAvailable(stock.getAvailable() - item.getQuantity());
            stockRepository.save(stock);
        });

        // success: next step capture
        outboxService.enqueueSagaCommand(order.getId(), event.idempotencyKey, event.correlationId,
                SagaEvent.of(EventTypes.PAYMENT_CAPTURE_REQUESTED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

        outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                SagaEvent.of(EventTypes.STOCK_RESERVED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));
    }
}
