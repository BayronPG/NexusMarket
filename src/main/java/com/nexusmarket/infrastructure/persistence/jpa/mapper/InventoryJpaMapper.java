package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryJpaEntity;

/**
 * Explicit mapper between the Inventory domain aggregate and its JPA entity.
 *
 * <p>{@code toDomain} uses the domain setters, so the domain invariant of
 * non-negative stock (RN-10) is preserved on the way back. Traceability: OBJ-06.</p>
 */
public final class InventoryJpaMapper {

    private InventoryJpaMapper() {
    }

    public static InventoryJpaEntity toJpa(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryJpaEntity entity = new InventoryJpaEntity();
        entity.setId(inventory.getId());
        entity.setProductId(inventory.getProductId());
        entity.setWarehouseId(inventory.getWarehouseId());
        entity.setQuantity(inventory.getQuantity());
        entity.setDamagedQuantity(inventory.getDamagedQuantity());
        return entity;
    }

    public static Inventory toDomain(InventoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Inventory inventory = new Inventory();
        inventory.setId(entity.getId());
        inventory.setProductId(entity.getProductId());
        inventory.setWarehouseId(entity.getWarehouseId());
        inventory.setQuantity(entity.getQuantity());
        inventory.setDamagedQuantity(entity.getDamagedQuantity());
        return inventory;
    }
}
