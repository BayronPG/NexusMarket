package com.nexusmarket.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity for the Seller aggregate.
 *
 * <p>{@code userId} mirrors the domain attribute
 * {@code com.nexusmarket.domain.seller.Seller#userId}, documented as an
 * INTERPRETATION that links the seller to its user. It is mapped as a scalar
 * identifier column, not as a JPA relationship (see Fase 1.2 report).</p>
 *
 * <p>Traceability: OBJ-02, RN-04 (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "sellers")
public class SellerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
