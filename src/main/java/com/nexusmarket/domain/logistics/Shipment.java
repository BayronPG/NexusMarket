package com.nexusmarket.domain.logistics;

/**
 * Shipping process of an order.
 *
 * <p>EXPLICIT source: component "procesos logísticos para productos
 * físicos" (OBJ-10); the flow includes packing, dispatch and transport, and
 * the order finishes after the delivery is confirmed.</p>
 *
 * <p>INTERPRETATION: {@code orderId} links the shipment to its order. The
 * specific attributes of a shipment are PENDING: the specification does not
 * detail them, so none are invented.</p>
 *
 * <p>Traceability: OBJ-10.</p>
 */
public class Shipment {

    /** Unique identifier of the shipment. */
    private Long id;

    /** Order being shipped; INTERPRETATION. */
    private Long orderId;

    public Shipment() {
    }

    public Shipment(Long id, Long orderId) {
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
