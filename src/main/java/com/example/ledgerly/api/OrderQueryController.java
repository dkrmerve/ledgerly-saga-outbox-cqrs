package com.example.ledgerly.api;

import com.example.ledgerly.api.dto.OrderViewDto;
import com.example.ledgerly.api.dto.OrderWriteDto;
import com.example.ledgerly.domain.order.Order;
import com.example.ledgerly.infra.db.repository.OrderRepository;
import com.example.ledgerly.infra.projection.mongo.OrderViewRepository;
import com.example.ledgerly.infra.projection.mongo.OrderViewDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/query")
public class OrderQueryController {

    private final OrderRepository orderRepository;
    private final OrderViewRepository orderViewRepository;

    public OrderQueryController(OrderRepository orderRepository, OrderViewRepository orderViewRepository) {
        this.orderRepository = orderRepository;
        this.orderViewRepository = orderViewRepository;
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getWriteModel(@PathVariable long id) {
        Order o = orderRepository.findById(id).orElseThrow();
        OrderWriteDto dto = new OrderWriteDto();
        dto.id = o.getId();
        dto.externalOrderId = o.getExternalOrderId();
        dto.status = o.getStatus().name();
        dto.total = o.getTotalAmount() + " " + o.getCurrency();
        dto.version = o.getVersion();
        dto.paymentAuthId = o.getPaymentAuthId();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/order-views/{id}")
    public ResponseEntity<?> getReadModel(@PathVariable long id) {
        OrderViewDocument v = orderViewRepository.findById(String.valueOf(id)).orElseThrow();
        OrderViewDto dto = new OrderViewDto();
        dto.orderId = v.getOrderId();
        dto.status = v.getStatus();
        dto.lastEventType = v.getLastEventType();
        dto.version = v.getVersion();
        dto.updatedAt = v.getUpdatedAt();
        return ResponseEntity.ok(dto);
    }
}
