package com.nexusmarket.domain.order;

/**
 * Lifecycle status of an order.
 *
 * <p>EXPLICIT source: ciclo de estados del pedido — Carrito, Pendiente de
 * Pago, Pagado, Despachado, Entregado/Finalizado. No additional transitions
 * are defined (RN-16).</p>
 *
 * <p>Traceability: OBJ-08, RN-16.</p>
 */
public enum OrderStatus {
    /** Provisional product selection. */
    CART,
    /** Waiting for financial confirmation. */
    PENDING_PAYMENT,
    /** Payment validated; preparation begins. */
    PAID,
    /** Physical departure from the warehouse. */
    SHIPPED,
    /** Successful delivery conclusion. */
    DELIVERED
}
