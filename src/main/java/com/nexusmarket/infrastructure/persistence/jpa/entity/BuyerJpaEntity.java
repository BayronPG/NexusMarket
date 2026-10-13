package com.nexusmarket.infrastructure.persistence.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for the Buyer aggregate.
 *
 * <p>Mirrors {@code com.nexusmarket.domain.buyer.Buyer}. The explicit list
 * attribute {@code additionalAddresses} is mapped with {@code @ElementCollection}
 * (an auxiliary table for the list itself, not a domain relationship).
 * {@code userId} is a scalar identifier column.</p>
 *
 * <p>Traceability: OBJ-03, RN-05 (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "buyers")
public class BuyerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "primary_address")
    private String primaryAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "buyer_additional_addresses",
            joinColumns = @JoinColumn(name = "buyer_id")
    )
    @Column(name = "address")
    private List<String> additionalAddresses = new ArrayList<>();

    @Column(name = "commercial_status")
    private String commercialStatus;

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

    public String getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(String primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public List<String> getAdditionalAddresses() {
        return additionalAddresses;
    }

    public void setAdditionalAddresses(List<String> additionalAddresses) {
        this.additionalAddresses = additionalAddresses;
    }

    public String getCommercialStatus() {
        return commercialStatus;
    }

    public void setCommercialStatus(String commercialStatus) {
        this.commercialStatus = commercialStatus;
    }
}
