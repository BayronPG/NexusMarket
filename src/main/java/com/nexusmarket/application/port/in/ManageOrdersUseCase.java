package com.nexusmarket.application.port.in;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;

/**
 * Inbound port: management of the order lifecycle and its lines.
 *
 * <p>EXPLICIT source: order management is a responsibility of the Buyer, the
 * Seller and the Logistics Operator (responsibilities matrix). An order starts
 * as a cart (OBJ-07) and follows the cycle Carrito, Pendiente de Pago, Pagado,
 * Despachado, Entregado/Finalizado without additional transitions (RN-16); a
 * finalized order cannot be modified (RN-13).</p>
 *
 * <p>INTERPRETATION: only the buyer that owns the cart edits its lines; only
 * published products with a positive price can be added, and the quantity can
 * never exceed the available stock. The unit price is a snapshot taken when the
 * line is created.</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08, RN-13, RN-16, RG-03.</p>
 */
public interface ManageOrdersUseCase {

    /**
     * Creates a new empty cart for the buyer identified by the actor.
     *
     * @param actor authenticated actor; must hold the BUYER role
     * @return the created order in CART status
     */
    Order createOrder(ActorContext actor);

    /**
     * Adds a published product to the cart, or increases its quantity when the
     * product is already present.
     *
     * @param actor     authenticated actor; owner of the cart and BUYER
     * @param orderId   order identifier
     * @param productId product identifier
     * @param quantity  positive quantity
     * @return the updated order with its lines
     */
    Order addItem(ActorContext actor, Long orderId, Long productId, int quantity);

    /**
     * Changes the quantity of a line of the cart.
     *
     * @param actor    authenticated actor; owner of the cart and BUYER
     * @param orderId  order identifier
     * @param itemId   line identifier
     * @param quantity new positive quantity
     * @return the updated order with its lines
     */
    Order updateItem(ActorContext actor, Long orderId, Long itemId, int quantity);

    /**
     * Removes a line of the cart.
     *
     * @param actor   authenticated actor; owner of the cart and BUYER
     * @param orderId order identifier
     * @param itemId  line identifier
     * @return the updated order with its lines
     */
    Order removeItem(ActorContext actor, Long orderId, Long itemId);

    /**
     * Moves an order to its next status.
     *
     * <p>An empty cart cannot move to PENDING_PAYMENT or PAID. The transition
     * to PAID deducts inventory once; repeating PAID is idempotent.</p>
     *
     * @param actor   authenticated actor; BUYER, SELLER or LOGISTICS_OPERATOR
     * @param orderId order identifier
     * @param next    next status; the domain validates the transition (RN-16)
     * @return the updated order
     */
    Order changeOrderStatus(ActorContext actor, Long orderId, OrderStatus next);

    /**
     * Ships one line of a paid order. Only the seller that owns the product of
     * that line can ship it; the global order status advances when every line
     * has been shipped.
     *
     * @param actor   authenticated SELLER that owns the product
     * @param orderId order identifier
     * @param itemId  line identifier
     * @return the updated order with its lines
     */
    Order shipItem(ActorContext actor, Long orderId, Long itemId);

    /**
     * Confirms the delivery of one shipped line. Only the logistics operator
     * performs this step; the global order status advances when every line has
     * been delivered.
     *
     * @param actor   authenticated LOGISTICS_OPERATOR
     * @param orderId order identifier
     * @param itemId  line identifier
     * @return the updated order with its lines
     */
    Order deliverItem(ActorContext actor, Long orderId, Long itemId);

    /**
     * Finds an order by its identifier, including its lines and total.
     *
     * @param actor   authenticated actor; a buyer only sees its own orders
     * @param orderId order identifier
     * @return the order
     */
    Order findOrder(ActorContext actor, Long orderId);
}
