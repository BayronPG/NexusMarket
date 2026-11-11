package com.nexusmarket.application.port.out;

import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Return;
import java.util.List;
import java.util.Optional;

/**
 * Output port for return persistence.
 *
 * <p>Traceability: OBJ-11.</p>
 */
public interface ReturnRepository {

    /** Stores the return, assigning an identifier when it is new. */
    Return save(Return returnRecord);

    /** Finds a return by its identifier. */
    Optional<Return> findById(Long id);

    /**
     * Locks and finds a return by its identifier.
     *
     * <p>The JPA adapter takes a pessimistic write lock, so the caller must run
     * inside a transaction. Used by reception to avoid restoring stock twice
     * under concurrent retries.</p>
     */
    Optional<Return> findByIdForUpdate(Long id);

    /** Lists every return of a line; used to accumulate previous quantities. */
    List<Return> findByOrderItemId(Long orderItemId);

    /** Lists the returns of an order. */
    PageResult<Return> findByOrderId(Long orderId, PageQuery query);
}
