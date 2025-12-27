package com.example.ledgerly.application.order;

import com.example.ledgerly.application.outbox.OutboxService;
import com.example.ledgerly.domain.event.EventTypes;
import com.example.ledgerly.domain.event.SagaEvent;
import com.example.ledgerly.domain.money.Money;
import com.example.ledgerly.domain.order.Order;
import com.example.ledgerly.domain.order.OrderItem;
import com.example.ledgerly.infra.db.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    public OrderService(OrderRepository orderRepository, OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
    }

    @Transactional
    public Order createAndStartSaga(String externalOrderId, String idempotencyKey, Money total, Iterable<OrderItem> items) {
        Order order = new Order();
        order.setExternalOrderId(externalOrderId);
        order.setIdempotencyKey(idempotencyKey);
        order.setTotal(total);
        order.touchNew();
        order.initializeAsPendingPayment();

        for (OrderItem it : items) {
            order.addItem(it);
        }

        Order saved = orderRepository.save(order);

        UUID correlationId = UUID.randomUUID();

        // Start saga
        outboxService.enqueueSagaCommand(
                saved.getId(),
                idempotencyKey,
                correlationId,
                SagaEvent.of(EventTypes.PAYMENT_AUTH_REQUESTED, saved.getId(), saved.getVersion(), correlationId, idempotencyKey)
        );

        return saved;
    }
}
