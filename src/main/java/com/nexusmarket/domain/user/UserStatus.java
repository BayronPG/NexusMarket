package com.nexusmarket.domain.user;

/**
 * Operational status of a user.
 *
 * <p>EXPLICIT source: "Condición operativa (Activo, Bloqueado, etc.)".
 * PENDING: the exact catalog of statuses is not defined in the specification;
 * only the two values cited are declared here.</p>
 *
 * <p>Traceability: OBJ-01.</p>
 */
public enum UserStatus {
    /** User can operate in the platform. */
    ACTIVE,
    /** User cannot operate until unblocked. */
    BLOCKED
}
