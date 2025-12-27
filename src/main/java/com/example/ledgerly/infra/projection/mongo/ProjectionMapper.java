package com.example.ledgerly.infra.projection.mongo;

import com.example.ledgerly.domain.event.SagaEvent;

import java.time.Instant;

public final class ProjectionMapper {
    private ProjectionMapper() {}

    public static void apply(OrderViewDocument doc, SagaEvent e) {
        doc.setOrderId(e.orderId);
        doc.setStatus(mapStatus(e.eventType, doc.getStatus()));
        doc.setLastEventType(e.eventType);
        doc.setVersion(Math.max(doc.getVersion(), e.orderVersion));
        doc.setUpdatedAt(Instant.now().toString());
    }

    private static String mapStatus(String eventType, String currentStatus) {
        switch (eventType) {
            case com.example.ledgerly.domain.event.EventTypes.ORDER_COMPLETED:
                return "COMPLETED";
            case com.example.ledgerly.domain.event.EventTypes.ORDER_CANCELLED:
                return "CANCELLED";
            default:
                return currentStatus != null ? currentStatus : "IN_PROGRESS";
        }
    }
}
