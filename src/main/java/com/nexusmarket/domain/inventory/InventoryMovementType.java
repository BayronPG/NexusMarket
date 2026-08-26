package com.nexusmarket.domain.inventory;

/**
 * Type of inventory movement.
 *
 * <p>EXPLICIT source: "Movimientos: Ingreso, Reserva, Salida por venta,
 * Ajuste y Devolución".</p>
 *
 * <p>Traceability: OBJ-06.</p>
 */
public enum InventoryMovementType {
    /** Stock entry. */
    INBOUND,
    /** Stock reserved for an order. */
    RESERVATION,
    /** Stock leaving because of a sale. */
    SALE_OUT,
    /** Manual stock adjustment. */
    ADJUSTMENT,
    /** Stock returning (devolution). */
    RETURN
}
