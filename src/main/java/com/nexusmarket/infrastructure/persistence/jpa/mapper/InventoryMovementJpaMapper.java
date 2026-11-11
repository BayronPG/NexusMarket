package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.inventory.InventoryMovement;
import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryMovementJpaEntity;

/**
 * Explicit mapper between the inventory movement and its JPA entity.
 *
 * <p>Traceability: OBJ-06, OBJ-11.</p>
 */
public final class InventoryMovementJpaMapper {

    private InventoryMovementJpaMapper() {
    }

    public static InventoryMovementJpaEntity toJpa(InventoryMovement movement) {
        if (movement == null) {
            return null;
        }
        InventoryMovementJpaEntity entity = new InventoryMovementJpaEntity();
        entity.setId(movement.getId());
        entity.setInventoryId(movement.getInventoryId());
        entity.setType(movement.getType());
        entity.setQuantity(movement.getQuantity());
        entity.setOccurredAt(movement.getOccurredAt());
        return entity;
    }

    public static InventoryMovement toDomain(InventoryMovementJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        InventoryMovement movement = new InventoryMovement(entity.getId(), entity.getInventoryId(),
                entity.getType(), entity.getQuantity());
        movement.setOccurredAt(entity.getOccurredAt());
        return movement;
    }
}
