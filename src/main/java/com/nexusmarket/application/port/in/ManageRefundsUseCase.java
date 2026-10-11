package com.nexusmarket.application.port.in;

import com.nexusmarket.application.ActorContext;
import com.nexusmarket.domain.returns.Refund;

/**
 * Inbound port: management of refunds.
 *
 * <p>EXPLICIT source: refund management is a responsibility of the Buyer and
 * the Administrator (responsibilities matrix, OBJ-11).</p>
 *
 * <p>Traceability: OBJ-11, RG-03.</p>
 */
public interface ManageRefundsUseCase {

    /**
     * Input data to request a refund.
     *
     * @param orderId required order being refunded
     */
    record RequestRefundCommand(Long orderId) {
    }

    /**
     * Registers a refund associated with an order.
     *
     * @param actor   authenticated actor; BUYER or ADMINISTRATOR
     * @param command input data
     * @return the created refund
     */
    Refund requestRefund(ActorContext actor, RequestRefundCommand command);
}
