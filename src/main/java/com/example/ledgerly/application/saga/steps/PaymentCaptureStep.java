package com.example.ledgerly.application.saga.steps;

import com.example.ledgerly.application.outbox.OutboxService;
import com.example.ledgerly.domain.event.EventTypes;
import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.infra.db.repository.OrderRepository;
import com.example.ledgerly.infra.payment.MasterpassClient;
import com.example.ledgerly.infra.payment.dto.MasterpassCaptureResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCaptureStep {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final MasterpassClient masterpassClient;

    public PaymentCaptureStep(OrderRepository orderRepository,
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
            throw new IllegalStateException("Missing paymentAuthId for capture");
        }

        MasterpassCaptureResponse res = masterpassClient.capture(authId, event.idempotencyKey);

        if ("CAPTURED".equalsIgnoreCase(res.getStatus())) {
            order.markCaptured();
            order.markCompleted();

            outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                    SagaEvent.of(EventTypes.ORDER_COMPLETED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

            return;
        }

        // capture fail -> void (best-effort)
        order.markCancelled("PAYMENT_CAPTURE_FAILED");

        outboxService.enqueueSagaCommand(order.getId(), event.idempotencyKey, event.correlationId,
                SagaEvent.of(EventTypes.PAYMENT_VOID_REQUESTED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));

        outboxService.enqueueDomainEvent(order.getId(), event.idempotencyKey, event.correlationId,
                SagaEvent.of(EventTypes.ORDER_CANCELLED, order.getId(), order.getVersion(), event.correlationId, event.idempotencyKey));
    }
}
