package com.example.ledgerly.application.saga.steps;

import com.example.ledgerly.application.outbox.OutboxService;
import com.example.ledgerly.domain.event.EventTypes;
import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.domain.order.Order;
import com.example.ledgerly.domain.order.OrderStatus;
import com.example.ledgerly.infra.db.repository.OrderRepository;
import com.example.ledgerly.infra.payment.MasterpassClient;
import com.example.ledgerly.infra.payment.dto.MasterpassAuthorizeResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentAuthorizeStep {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final MasterpassClient masterpassClient;

    public PaymentAuthorizeStep(
            OrderRepository orderRepository,
            OutboxService outboxService,
            MasterpassClient masterpassClient
    ) {
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
        this.masterpassClient = masterpassClient;
    }

    @Transactional
    public void handle(SagaEvent event) {
        Order order = orderRepository.findById(event.orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.orderId));

        MasterpassAuthorizeResponse auth = masterpassClient.authorize(
                order.getId(),
                order.getTotalAmount().toString(),
                order.getCurrency(),
                event.idempotencyKey
        );

        if ("AUTHORIZED".equalsIgnoreCase(auth.getStatus())) {
            order.markPaymentAuthorized(auth.getAuthId());

            // next saga command
            outboxService.enqueueSagaCommand(order.getId(), event.idempotencyKey, event.correlationId,
                    SagaEvent.of(EventTypes.STOCK_RESERVE_REQUESTED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

            // optional domain event
            outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                    SagaEvent.of(EventTypes.PAYMENT_AUTHORIZED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

            return;
        }

        // payment failed
        order.markPaymentFailed("PAYMENT_AUTHORIZATION_FAILED");

        outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                SagaEvent.of(EventTypes.PAYMENT_FAILED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

        // cancel order in demo
        outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                SagaEvent.of(EventTypes.ORDER_CANCELLED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));
    }
}
