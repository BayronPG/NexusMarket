package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.order.Order;
import java.util.List;
import java.util.Optional;

/**
 * Output port for order persistence.
 *
 * <p>Traceability: OBJ-08.</p>
 */
public interface OrderRepository {

    /** Stores the order, assigning an identifier when it is new. */
    Order save(Order order);

    /** Finds an order by its identifier. */
    Optional<Order> findById(Long id);

    /**
     * Finds and locks an order for update.
     *
     * <p>The JPA adapter takes a pessimistic write lock, so the caller must run
     * inside a transaction. Used by the status change to serialize concurrent
     * payments of the same order.</p>
     */
    Optional<Order> findByIdForUpdate(Long id);

    /** Lists the orders created by a buyer. */
    List<Order> findByBuyerId(Long buyerId);
}
