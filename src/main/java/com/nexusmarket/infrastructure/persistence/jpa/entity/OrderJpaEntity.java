package com.nexusmarket.infrastructure.persistence.jpa.entity;

import com.nexusmarket.domain.order.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity for the Order aggregate.
 *
 * <p>Mirrors {@code com.nexusmarket.domain.order.Order}. {@code buyerId} is a
 * scalar identifier column (documented in the domain as an INTERPRETATION),
 * not a JPA relationship. Table name {@code orders} avoids the SQL reserved
 * word "order".</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08, RN-13, RN-16 (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "buyer_id")
    private Long buyerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
