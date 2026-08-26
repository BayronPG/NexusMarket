package com.nexusmarket.domain.billing;

/**
 * Invoicing of a purchase.
 *
 * <p>EXPLICIT source: component "información comercial asociada a las
 * ventas" (OBJ-09).</p>
 *
 * <p>INTERPRETATION: {@code orderId} links the invoice to the order that
 * originated the sale. The specific attributes of an invoice are PENDING:
 * the specification does not detail them, so none are invented.</p>
 *
 * <p>Traceability: OBJ-09.</p>
 */
public class Invoice {

    /** Unique identifier of the invoice. */
    private Long id;

    /** Order associated with the sale; INTERPRETATION. */
    private Long orderId;

    public Invoice() {
    }

    public Invoice(Long id, Long orderId) {
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
