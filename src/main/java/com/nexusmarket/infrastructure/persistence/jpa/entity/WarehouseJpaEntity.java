package com.nexusmarket.infrastructure.persistence.jpa.entity;

import com.nexusmarket.domain.warehouse.WarehouseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity for the Warehouse aggregate.
 *
 * <p>Mirrors {@code com.nexusmarket.domain.warehouse.Warehouse}.
 * {@code sellerId} is nullable in the domain (null for MARKETPLACE warehouses);
 * it is mapped as a nullable scalar identifier column, not as a JPA relationship.</p>
 *
 * <p>Traceability: OBJ-04 (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "warehouses")
public class WarehouseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private WarehouseType type;

    @Column(name = "seller_id", nullable = true)
    private Long sellerId;

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
