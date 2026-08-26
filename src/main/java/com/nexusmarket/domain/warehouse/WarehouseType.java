package com.nexusmarket.domain.warehouse;

/**
 * Classification of a warehouse.
 *
 * <p>EXPLICIT source: the specification distinguishes "bodegas del
 * Marketplace" and "bodegas de Vendedores".</p>
 *
 * <p>Traceability: OBJ-04.</p>
 */
public enum WarehouseType {
    /** Warehouse owned by the Marketplace. */
    MARKETPLACE,
    /** Warehouse owned by a seller. */
    SELLER
}
