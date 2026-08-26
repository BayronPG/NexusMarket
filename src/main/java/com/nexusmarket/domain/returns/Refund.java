package com.nexusmarket.domain.returns;

/**
 * Refund of a purchase.
 *
 * <p>EXPLICIT source: refunds are an included process (OBJ-11); per the
 * responsibilities matrix, refund management is performed by the Buyer and
 * the Administrator.</p>
 *
 * <p>INTERPRETATION: {@code orderId} links the refund to its order. The
 * specific attributes of a refund are PENDING: the specification does not
 * detail them, so none are invented.</p>
 *
 * <p>Traceability: OBJ-11.</p>
 */
public class Refund {

    /** Unique identifier of the refund. */
    private Long id;

    /** Order being refunded; INTERPRETATION. */
    private Long orderId;

    public Refund() {
    }

    public Refund(Long id, Long orderId) {
        this.id = id;
        this.orderId = orderId;
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
}
