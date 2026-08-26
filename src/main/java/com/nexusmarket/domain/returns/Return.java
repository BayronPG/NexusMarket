package com.nexusmarket.domain.returns;

/**
 * Devolution of a purchased product.
 *
 * <p>EXPLICIT source: devolutions are an included process (OBJ-11) and also
 * one of the inventory movement types (Devolución).</p>
 *
 * <p>INTERPRETATION: {@code orderId} links the devolution to its order. The
 * specific attributes of a devolution are PENDING: the specification does
 * not detail them, so none are invented.</p>
 *
 * <p>Traceability: OBJ-11.</p>
 */
public class Return {

    /** Unique identifier of the devolution. */
    private Long id;

    /** Order being returned; INTERPRETATION. */
    private Long orderId;

    public Return() {
    }

    public Return(Long id, Long orderId) {
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
