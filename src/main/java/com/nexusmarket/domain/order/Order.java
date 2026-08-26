package com.nexusmarket.domain.order;

import java.util.EnumMap;
import java.util.Map;

/**
 * Order of the NexusMarket platform.
 *
 * <p>EXPLICIT source: component "solicitudes de compra realizadas por
 * compradores"; formal commercial commitment with a defined lifecycle
 * (OBJ-08).</p>
 *
 * <p>INTERPRETATION: {@code buyerId} links the order to the buyer who made
 * it. The content of the order (items, quantities, prices) is PENDING: the
 * specification does not describe order lines.</p>
 *
 * <p>Business rules enforced here: only the defined lifecycle transitions
 * are allowed (RN-16) and a finalized order cannot be modified (RN-13).</p>
 *
 * <p>Traceability: OBJ-08, RN-13, RN-16.</p>
 */
public class Order {

    /** Unique identifier of the order. */
    private Long id;

    /** Buyer that made the order; INTERPRETATION. */
    private Long buyerId;

    /** Current lifecycle status. */
    private OrderStatus status;

    /** Allowed transitions defined by the specification (RN-16). */
    private static final Map<OrderStatus, OrderStatus> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.CART, OrderStatus.PENDING_PAYMENT);
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING_PAYMENT, OrderStatus.PAID);
        ALLOWED_TRANSITIONS.put(OrderStatus.PAID, OrderStatus.SHIPPED);
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    }

    public Order() {
    }

    public Order(Long id, Long buyerId, OrderStatus status) {
        this.id = id;
        this.buyerId = buyerId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Changes the order status only through the allowed lifecycle (RN-16).
     *
     * @param next the next status
     * @throws IllegalStateException if the order is finalized (RN-13) or the
     *                               transition is not allowed (RN-16)
     */
    public void changeStatus(OrderStatus next) {
        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("A finalized order cannot be modified (RN-13)");
        }
        OrderStatus allowedNext = ALLOWED_TRANSITIONS.get(status);
        if (allowedNext == null || allowedNext != next) {
            throw new IllegalStateException(
                    "Transition not allowed: " + status + " -> " + next + " (RN-16)");
        }
        this.status = next;
    }
}
