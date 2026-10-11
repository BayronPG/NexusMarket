package com.nexusmarket.application.port.in;

import com.nexusmarket.application.ActorContext;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;

/**
 * Inbound port: management of the order lifecycle.
 *
 * <p>EXPLICIT source: order management is a responsibility of the Buyer, the
 * Seller and the Logistics Operator (responsibilities matrix). An order starts
 * as a cart (OBJ-07) and follows the cycle Carrito, Pendiente de Pago, Pagado,
 * Despachado, Entregado/Finalizado without additional transitions (RN-16); a
 * finalized order cannot be modified (RN-13).</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08, RN-13, RN-16, RG-03.</p>
 */
public interface ManageOrdersUseCase {

    /**
     * Creates a new order for the buyer identified by the actor.
     *
     * <p>The content of the order (items, quantities, prices) is not defined
     * by the specification, so this increment only creates the order in its
     * initial CART status.</p>
     *
     * @param actor authenticated actor; must hold the BUYER role
     * @return the created order
     */
    Order createOrder(ActorContext actor);

    /**
     * Moves an order to its next status.
     *
     * @param actor   authenticated actor; BUYER, SELLER or LOGISTICS_OPERATOR
     * @param orderId order identifier
     * @param next    next status; the domain validates the transition (RN-16)
     * @return the updated order
     */
    Order changeOrderStatus(ActorContext actor, Long orderId, OrderStatus next);

    /**
     * Finds an order by its identifier.
     *
     * @param actor   authenticated actor; BUYER, SELLER or LOGISTICS_OPERATOR
     * @param orderId order identifier
     * @return the order
     */
    Order findOrder(ActorContext actor, Long orderId);
}
