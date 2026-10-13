package com.nexusmarket.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity for the Inventory aggregate.
 *
 * <p>Mirrors {@code com.nexusmarket.domain.inventory.Inventory}. The documented
 * links to a product and a warehouse are stored as scalar identifier columns
 * ({@code productId}, {@code warehouseId}), exactly as the domain models them;
 * no JPA association is declared (see Fase 1.2 report).</p>
 *
 * <p>Traceability: OBJ-06, RN-09, RN-10, RN-11, RN-12 (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "inventories")
public class InventoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "damaged_quantity")
    private int damagedQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getDamagedQuantity() {
        return damagedQuantity;
    }

    public void setDamagedQuantity(int damagedQuantity) {
        this.damagedQuantity = damagedQuantity;
    }
}
