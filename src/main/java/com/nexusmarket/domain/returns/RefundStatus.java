package com.nexusmarket.domain.returns;

/**
 * State of a partial refund.
 *
 * <p>There is no payment gateway: REGISTERED means the refund obligation was
 * recorded with the historical unit price, not that money was transferred.</p>
 *
 * <p>Traceability: OBJ-11.</p>
 */
public enum RefundStatus {
    /** Refund recorded; no external money movement is integrated. */
    REGISTERED
}
