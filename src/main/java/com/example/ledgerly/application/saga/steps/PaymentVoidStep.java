package com.example.ledgerly.application.saga.steps;

import com.example.ledgerly.application.outbox.OutboxService;
import com.example.ledgerly.domain.event.EventTypes;
import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.infra.db.repository.OrderRepository;
import com.example.ledgerly.infra.payment.MasterpassClient;
import com.example.ledgerly.infra.payment.dto.MasterpassVoidResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentVoidStep {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final MasterpassClient masterpassClient;

    public PaymentVoidStep(OrderRepository orderRepository,
                           OutboxService outboxService,
                           MasterpassClient masterpassClient) {
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
        this.masterpassClient = masterpassClient;
    }

    @Transactional
    public void handle(SagaEvent event) {
        var order = orderRepository.findById(event.orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + event.orderId));

        String authId = order.getPaymentAuthId();
        if (authId == null || authId.isBlank()) {
            // nothing to void; idempotent
            return;
        }

        MasterpassVoidResponse res = masterpassClient.voidAuth(authId, event.idempotencyKey);

        // We don't block on status in demo; best-effort
        outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                SagaEvent.of(EventTypes.ORDER_CANCELLED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));
    }
}
