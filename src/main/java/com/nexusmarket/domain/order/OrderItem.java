package com.nexusmarket.domain.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Line of an order: product, quantity and the unit price snapshot.
 *
 * <p>INTERPRETATION: the specification does not describe order lines; this
 * increment implements them with the minimum content required to calculate the
 * order total. {@code unitPrice} is a snapshot of the product price at the
 * moment the item is added, so later catalog changes never alter it.</p>
 *
 * <p>Business rules: quantity is always positive; the unit price is positive
 * and stored with two decimals (HALF_UP).</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08.</p>
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

    public OrderItem() {
    }

    public OrderItem(Long id, Long orderId, Long productId, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        setQuantity(quantity);
        setUnitPrice(unitPrice);
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
     * @throws IllegalArgumentException if the price is null or not positive
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("The unit price of an order item must be positive");
        }
        this.unitPrice = unitPrice.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
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
}
