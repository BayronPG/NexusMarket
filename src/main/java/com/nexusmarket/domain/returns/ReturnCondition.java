package com.nexusmarket.domain.returns;

/**
 * Physical condition of a returned unit.
 *
 * <p>Decides where the returned quantity is recorded: RESELLABLE increases the
 * available stock, DAMAGED increases the damaged quantity so the units are not
 * offered again.</p>
 *
 * <p>Traceability: OBJ-11, RN-12.</p>
 */
public enum ReturnCondition {
    /** The unit can be sold again. */
    RESELLABLE,
    /** The unit is damaged and must not be available. */
    DAMAGED
}
