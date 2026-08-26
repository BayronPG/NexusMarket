package com.nexusmarket.domain.user;

/**
 * Role that a user can hold in NexusMarket.
 *
 * <p>EXPLICIT source: the specification defines five participants, each with a
 * single role (RG-02).</p>
 *
 * <p>Traceability: OBJ-01, RG-02.</p>
 */
public enum Role {
    /** Person who purchases published products. */
    BUYER,
    /** Responsible for registering and managing their products. */
    SELLER,
    /** In charge of the physical operation of warehouses and shipments. */
    LOGISTICS_OPERATOR,
    /** Responsible for the administration of sellers and warehouses. */
    ADMINISTRATOR,
    /** Read-only operational monitoring profile. */
    SUPERVISOR
}
