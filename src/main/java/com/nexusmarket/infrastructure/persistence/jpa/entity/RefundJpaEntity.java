package com.nexusmarket.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity for the Refund aggregate.
 *
 * <p>Mirrors {@code com.nexusmarket.domain.returns.Refund}. {@code orderId} is
 * a scalar identifier column (documented in the domain as an INTERPRETATION),
 * not a JPA relationship. The specification does not detail refund attributes,
 * so none are invented.</p>
 *
 * <p>Traceability: OBJ-11 (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "refunds")
public class RefundJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
