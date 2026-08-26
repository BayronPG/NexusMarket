package com.nexusmarket.domain.warehouse;

/**
 * Warehouse of the NexusMarket platform.
 *
 * <p>EXPLICIT source: component "lugares donde se administra el inventario
 * físico"; classified as Marketplace warehouses and Seller warehouses
 * (OBJ-04).</p>
 *
 * <p>INTERPRETATION: {@code sellerId} is present because "bodegas de
 * Vendedores" implies ownership by a seller; it is null for Marketplace
 * warehouses. The first warehouse of a seller is registered by the
 * Administrator (flujo general, paso 1).</p>
 *
 * <p>Traceability: OBJ-04.</p>
 */
public class Warehouse {

    /** Unique identifier of the warehouse. */
    private Long id;

    /** Classification of the warehouse; required. */
    private WarehouseType type;

    /** Seller that owns the warehouse; INTERPRETATION, null for MARKETPLACE. */
    private Long sellerId;

    public Warehouse() {
    }

    public Warehouse(Long id, WarehouseType type) {
        this.id = id;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WarehouseType getType() {
        return type;
    }

    public void setType(WarehouseType type) {
        this.type = type;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
}
