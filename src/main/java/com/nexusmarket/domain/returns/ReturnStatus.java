package com.nexusmarket.domain.returns;

/**
 * Lifecycle of a return request.
 *
 * <p>Increment "envío y devolución por ítem" (2026-11-10): the buyer requests,
 * the administrator approves, the logistics operator receives. REJECTED is
 * declared but has no endpoint in the minimal API.</p>
 *
 * <p>Traceability: OBJ-11.</p>
 */
public enum ReturnStatus {
    /** Requested by the buyer; waiting for approval. */
    REQUESTED,
    /** Approved; waiting for the physical reception. */
    APPROVED,
    /** Physically received; the stock and the refund were registered. */
    RECEIVED,
    /** Rejected; no endpoint exposes this state in the minimal API. */
    REJECTED
}
