package com.nexusmarket.domain.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Line of an order: product, quantity, the unit price snapshot and the
 * fulfillment state.
 *
 * <p>INTERPRETATION: the specification does not describe order lines; this
 * increment implements them with the minimum content required to calculate the
 * order total and to dispatch/return each product independently.
 * {@code unitPrice} is a snapshot of the product price at the moment the item
 * is added, so later catalog changes never alter it.</p>
 *
 * <p>Business rules: quantity is always positive; the unit price is positive
 * and stored with two decimals (HALF_UP); the fulfillment state only moves
 * forward through the defined sequence (PENDING, SHIPPED, DELIVERED,
 * RETURN_REQUESTED, RETURNED, REFUNDED).</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08, OBJ-10, OBJ-11.</p>
 */
public class OrderItem {

    /** Monetary scale used by the order model. */
    public static final int PRICE_SCALE = 2;

    private Long id;

    /** Order that owns the line. */
    private Long orderId;

    /** Referenced product. */
    private Long productId;

    /** Positive quantity of units. */
    private int quantity;

    /** Price snapshot at the moment the product was added. */
    private BigDecimal unitPrice;

    /** Logistics state of this line; starts as PENDING. */
    private OrderItemStatus status = OrderItemStatus.PENDING;

    /**
     * Seller that owns the referenced product. Derived from Product when the
     * order is read; it is not a persisted column (the link is indirect).
     */
    private Long sellerId;

    public OrderItem() {
    }

    public OrderItem(Long id, Long orderId, Long productId, int quantity, BigDecimal unitPrice) {
        this(id, orderId, productId, quantity, unitPrice, OrderItemStatus.PENDING);
    }

    /**
     * Hydration constructor that also restores the fulfillment state.
     *
     * <p>Used by the persistence mapper; domain code must use the transition
     * methods instead of setting the state directly.</p>
     */
    public OrderItem(Long id, Long orderId, Long productId, int quantity, BigDecimal unitPrice,
                     OrderItemStatus status) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        setQuantity(quantity);
        setUnitPrice(unitPrice);
        this.status = status == null ? OrderItemStatus.PENDING : status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity; it must be positive.
     *
     * @param quantity new quantity
     * @throws IllegalArgumentException if the quantity is not positive
     */
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("The quantity of an order item must be positive");
        }
        this.quantity = quantity;
    }

    /**
     * Increases the quantity of an existing line.
     *
     * @param amount units to add; must be positive
     * @throws IllegalArgumentException if the amount is not positive
     */
    public void increase(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The quantity to add must be positive");
        }
        this.quantity += amount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets the unit price; it must be positive.
     *
     * @param unitPrice price snapshot
     * @throws IllegalArgumentException if the unitPrice is null or not positive
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("The unit price of an order item must be positive");
        }
        this.unitPrice = unitPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /** Current fulfillment state. */
    public OrderItemStatus getStatus() {
        return status;
    }

    /** Seller of the product; derived on read, not persisted. */
    public Long getSellerId() {
        return sellerId;
    }

    /** Sets the derived seller identifier; ignored by persistence. */
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    /**
     * Subtotal of the line: {@code quantity * unitPrice}.
     *
     * @return subtotal with two decimals
     */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Marks a pending line as shipped.
     *
     * @throws IllegalStateException if the line is not PENDING
     */
    public void ship() {
        transition(OrderItemStatus.PENDING, OrderItemStatus.SHIPPED,
                "Only a pending line can be shipped");
    }

    /**
     * Marks a shipped line as delivered.
     *
     * @throws IllegalStateException if the line is not SHIPPED
     */
    public void deliver() {
        transition(OrderItemStatus.SHIPPED, OrderItemStatus.DELIVERED,
                "Only a shipped line can be delivered");
    }

    /**
     * Marks a delivered line as returned-requested.
     *
     * @throws IllegalStateException if the line is not DELIVERED
     */
    public void requestReturn() {
        transition(OrderItemStatus.DELIVERED, OrderItemStatus.RETURN_REQUESTED,
                "Only a delivered line can be returned");
    }

    /**
     * Marks a return-requested line as physically returned.
     *
     * @throws IllegalStateException if the line is not RETURN_REQUESTED
     */
    public void receiveReturn() {
        transition(OrderItemStatus.RETURN_REQUESTED, OrderItemStatus.RETURNED,
                "Only a line with a return request can be received");
    }

    /**
     * Marks a returned line as refunded.
     *
     * @throws IllegalStateException if the line is not RETURNED
     */
    public void refund() {
        transition(OrderItemStatus.RETURNED, OrderItemStatus.REFUNDED,
                "Only a received line can be refunded");
    }

    private void transition(OrderItemStatus expected, OrderItemStatus next, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
        this.status = next;
    }
}
