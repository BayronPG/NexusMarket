package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.inventory.InventoryMovement;

/**
 * Output port for inventory movement persistence.
 *
 * <p>Traceability: OBJ-06, OBJ-11.</p>
 */
public interface InventoryMovementRepository {

    /** Stores the movement, assigning an identifier when it is new. */
    InventoryMovement save(InventoryMovement movement);
}
