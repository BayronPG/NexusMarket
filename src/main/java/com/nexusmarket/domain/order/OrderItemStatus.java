package com.nexusmarket.domain.order;

/**
 * Fulfillment state of an order line.
 *
 * <p>Increment "envío y devolución por ítem" (2026-11-10): an order may contain
 * products from several sellers, so the logistics state belongs to the line,
 * not only to the order. The sequence is PENDING, SHIPPED, DELIVERED,
 * RETURN_REQUESTED, RETURNED, REFUNDED.</p>
 *
 * <p>Traceability: OBJ-10, OBJ-11.</p>
 */
public enum OrderItemStatus {

    /** Paid line waiting for the seller to dispatch it. */
    PENDING(0),
    /** Dispatched by the seller that owns the product. */
    SHIPPED(1),
    /** Delivery confirmed by the logistics operator. */
    DELIVERED(2),
    /** The buyer requested a return for this line. */
    RETURN_REQUESTED(3),
    /** The return was physically received. */
    RETURNED(4),
    /** The partial refund was registered. */
    REFUNDED(5);

    private final int rank;

    OrderItemStatus(int rank) {
        this.rank = rank;
    }

    /** Whether this state is the same or more advanced than {@code other}. */
    public boolean isAtLeast(OrderItemStatus other) {
        return rank >= other.rank;
    }

    /** Whether the line left the seller (SHIPPED or later). */
    public boolean isAtLeastShipped() {
        return isAtLeast(SHIPPED);
    }

    /** Whether the line was delivered (DELIVERED or later). */
    public boolean isAtLeastDelivered() {
        return isAtLeast(DELIVERED);
    }
}
