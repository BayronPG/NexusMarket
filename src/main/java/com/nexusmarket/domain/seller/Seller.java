package com.nexusmarket.domain.seller;

/**
 * Seller of the NexusMarket platform.
 *
 * <p>EXPLICIT source: participant "responsable de registrar y administrar sus
 * productos". Business rule: sellers cannot self-register; they are
 * incorporated by the Administrator (RN-04).</p>
 *
 * <p>INTERPRETATION: {@code userId} links the seller to its {@code User}
 * record, because each participant performs a single role (RG-02).</p>
 *
 * <p>Traceability: OBJ-02, RN-04.</p>
 */
public class Seller {

    /** Unique identifier of the seller. */
    private Long id;

    /** Identifier of the underlying user; INTERPRETATION. */
    private Long userId;

    public Seller() {
    }

    public Seller(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

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
