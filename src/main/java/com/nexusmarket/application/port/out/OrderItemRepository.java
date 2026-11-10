package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.order.OrderItem;
import java.util.List;
import java.util.Optional;

/**
 * Output port for order line persistence.
 *
 * <p>Traceability: OBJ-07, OBJ-08.</p>
 */
public interface OrderItemRepository {

    /** Stores the line, assigning an identifier when it is new. */
    OrderItem save(OrderItem item);

    /** Finds a line by its identifier. */
    Optional<OrderItem> findById(Long id);

    /** Lists the lines of an order, ordered by identifier. */
    List<OrderItem> findByOrderId(Long orderId);

    /** Deletes a line by its identifier. */
    void deleteById(Long id);
}
