package com.example.ledgerly.domain.order;

/**
 * Explicit state transitions:
 * reviewers can quickly validate that we don't allow illegal jumps.
 */
public final class OrderStateMachine {
    private OrderStateMachine() {}

    public static void assertTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case PENDING_PAYMENT:
                if (to == OrderStatus.PAYMENT_AUTHORIZED || to == OrderStatus.PAYMENT_FAILED) return;
                break;
            case PAYMENT_AUTHORIZED:
                if (to == OrderStatus.STOCK_RESERVED || to == OrderStatus.STOCK_RESERVE_FAILED) return;
                break;
            case STOCK_RESERVED:
                if (to == OrderStatus.CAPTURED || to == OrderStatus.CANCELLED) return;
                break;
            case CAPTURED:
                if (to == OrderStatus.COMPLETED) return;
                break;
            case PAYMENT_FAILED:
            case STOCK_RESERVE_FAILED:
                if (to == OrderStatus.CANCELLED) return;
                break;
            default:
                break;
        }
        throw new IllegalStateException("Illegal transition: " + from + " -> " + to);
    }
}
