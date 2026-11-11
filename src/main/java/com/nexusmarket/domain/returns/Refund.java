package com.nexusmarket.domain.returns;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Partial refund generated when a return is received.
 *
 * <p>Increment "envío y devolución por ítem" (2026-11-10): the amount is the
 * returned quantity multiplied by the historical unit price of the order line,
 * never by the current catalog price. One return produces at most one refund.</p>
 *
 * <p>Traceability: OBJ-11.</p>
 */
public class Refund {

    private Long id;

    /** Return that originated the refund; unique. */
    private Long returnId;

    /** Order that contains the refunded line. */
    private Long orderId;

    /** Refunded order line. */
    private Long orderItemId;

    /** Returned quantity multiplied by the historical unit price. */
    private BigDecimal amount;

    /** Refund state; no payment gateway is integrated. */
    private RefundStatus status;

    private LocalDateTime createdAt;

    public Refund() {
    }

    public Refund(Long id, Long returnId, Long orderId, Long orderItemId, BigDecimal amount,
                  RefundStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.returnId = returnId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        setAmount(amount);
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReturnId() {
        return returnId;
    }

    public void setReturnId(Long returnId) {
        this.returnId = returnId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the refunded amount; it cannot be null or negative.
     *
     * @param amount monetary amount
     * @throws IllegalArgumentException if the amount is null or negative
     */
    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("The refund amount cannot be negative");
        }
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
