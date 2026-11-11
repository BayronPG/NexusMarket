package com.nexusmarket.application.port.in;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Refund;

/**
 * Inbound port: read access to partial refunds.
 *
 * <p>Increment "envío y devolución por ítem" (2026-11-10): refunds are created
 * automatically when a return is received, so this port only exposes queries.</p>
 *
 * <p>Traceability: OBJ-11, RG-03.</p>
 */
public interface ManageRefundsUseCase {

    /**
     * Finds a refund.
     *
     * @param actor   authenticated buyer owner or administrator
     * @param refundId refund identifier
     * @return the refund
     */
    Refund findRefund(ActorContext actor, Long refundId);

    /**
     * Lists the refunds of an order.
     *
     * @param actor   authenticated buyer owner or administrator
     * @param orderId order identifier
     * @param query   pagination and sort
     * @return page of refunds
     */
    PageResult<Refund> findRefundsByOrder(ActorContext actor, Long orderId, PageQuery query);
}
