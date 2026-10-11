package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.returns.Refund;
import java.util.List;
import java.util.Optional;

/**
 * Output port for refund persistence.
 *
 * <p>Traceability: OBJ-11.</p>
 */
public interface RefundRepository {

    /** Stores the refund, assigning an identifier when it is new. */
    Refund save(Refund refund);

    /** Finds a refund by its identifier. */
    Optional<Refund> findById(Long id);

    /** Lists the refunds requested for an order. */
    List<Refund> findByOrderId(Long orderId);
}
