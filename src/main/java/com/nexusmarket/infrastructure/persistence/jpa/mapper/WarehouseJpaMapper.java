package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.infrastructure.persistence.jpa.entity.WarehouseJpaEntity;

/**
 * Explicit mapper between the Warehouse domain aggregate and its JPA entity.
 *
 * <p>{@code sellerId} may be null for MARKETPLACE warehouses. Traceability: OBJ-04.</p>
 */
public final class WarehouseJpaMapper {

    private WarehouseJpaMapper() {
    }

    public static WarehouseJpaEntity toJpa(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setId(warehouse.getId());
        entity.setType(warehouse.getType());
        entity.setSellerId(warehouse.getSellerId());
        return entity;
    }

    public static Warehouse toDomain(WarehouseJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setId(entity.getId());
        warehouse.setType(entity.getType());
        warehouse.setSellerId(entity.getSellerId());
        return warehouse;
    }
}
