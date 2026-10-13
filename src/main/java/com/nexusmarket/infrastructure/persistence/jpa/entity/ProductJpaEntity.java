package com.nexusmarket.infrastructure.persistence.jpa.entity;

import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for the Product aggregate.
 *
 * <p>Mirrors {@code com.nexusmarket.domain.catalog.Product}. The explicit list
 * attribute {@code variants} is mapped with {@code @ElementCollection} (an
 * auxiliary table for the list itself, not a domain relationship).
 * {@code sellerId} is a scalar identifier column.</p>
 *
 * <p>Traceability: OBJ-05, RN-06, RN-07, RN-08 (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_id")
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ProductType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_variants",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "variant")
    private List<String> variants = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public List<String> getVariants() {
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}
