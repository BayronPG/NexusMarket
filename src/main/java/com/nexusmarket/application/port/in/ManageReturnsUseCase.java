package com.nexusmarket.application.port.in;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.returns.Return;
import com.nexusmarket.domain.returns.ReturnCondition;

/**
 * Inbound port: returns and their partial refunds.
 *
 * <p>Increment "envío y devolución por ítem" (2026-11-10). Only the buyer that
 * owns the order requests a return; the administrator approves it and the
 * logistics operator receives it. Reception restores the stock once and
 * generates one refund with the historical unit price.</p>
 *
 * <p>Traceability: OBJ-11, RG-03, RN-12.</p>
 */
public interface ManageReturnsUseCase {

    /**
     * Input data to request the return of one order line.
     *
     * @param orderId     order identifier
     * @param orderItemId returned line
     * @param quantity    units to return; positive and within the purchased quantity
     * @param reason      motive declared by the buyer
     * @param condition   RESELLABLE or DAMAGED
     */
    record RequestReturnCommand(Long orderId, Long orderItemId, int quantity, String reason,
                                ReturnCondition condition) {
    }

    /**
     * Result of receiving a return: the updated return and its refund.
     *
     * @param returnRecord received return
     * @param refund       refund generated (or the existing one on a retry)
     */
    record ReturnReceipt(Return returnRecord, Refund refund) {
    }

    /**
     * Requests the return of a delivered line.
     *
     * @param actor   authenticated BUYER that owns the order
     * @param command input data
     * @return the created return in REQUESTED state
     */
    Return requestReturn(ActorContext actor, RequestReturnCommand command);

    /**
     * Approves a requested return.
     *
     * @param actor    authenticated ADMINISTRATOR
     * @param returnId return identifier
     * @return the return in APPROVED state
     */
    Return approveReturn(ActorContext actor, Long returnId);

    /**
     * Receives an approved return: restores the stock once, records the
     * movement and generates the partial refund. Repeating it returns the
     * existing refund without touching the stock again.
     *
     * @param actor    authenticated LOGISTICS_OPERATOR
     * @param returnId return identifier
     * @return the received return and its refund
     */
    ReturnReceipt receiveReturn(ActorContext actor, Long returnId);

    /**
     * Finds a return.
     *
     * @param actor    authenticated buyer owner or administrator
     * @param returnId return identifier
     * @return the return
     */
    Return findReturn(ActorContext actor, Long returnId);

    /**
     * Lists the returns of an order.
     *
     * @param actor   authenticated buyer owner or administrator
     * @param orderId order identifier
     * @param query   pagination and sort
     * @return page of returns
     */
    PageResult<Return> findReturnsByOrder(ActorContext actor, Long orderId, PageQuery query);
}
