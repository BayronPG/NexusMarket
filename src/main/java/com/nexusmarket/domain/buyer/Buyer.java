package com.nexusmarket.domain.buyer;

import java.util.ArrayList;
import java.util.List;

/**
 * Buyer of the NexusMarket platform.
 *
 * <p>EXPLICIT source: participant "persona que adquiere productos publicados".
 * Attributes: main address (required), additional addresses (optional) and
 * commercial status (required).</p>
 *
 * <p>INTERPRETATION: {@code userId} links the buyer to its {@code User}
 * record, because each participant performs a single role (RG-02) and the
 * buyer is a user. {@code commercialStatus} is kept as free text because the
 * specification does not define its catalog (PENDING).</p>
 *
 * <p>Restriction (EXPLICIT): the buyer never administers other buyers'
 * information nor inventories (RN-05).</p>
 *
 * <p>Traceability: OBJ-03, RN-05.</p>
 */
public class Buyer {

    /** Unique identifier of the buyer. */
    private Long id;

    /** Identifier of the underlying user; INTERPRETATION. */
    private Long userId;

    /** Usual delivery location; required. */
    private String primaryAddress;

    /** Secondary delivery locations; optional. */
    private List<String> additionalAddresses = new ArrayList<>();

    /** Commercial condition of the buyer to make purchases; required. Catalog PENDING. */
    private String commercialStatus;

    public Buyer() {
    }

    public Buyer(Long id, Long userId, String primaryAddress, String commercialStatus) {
        this.id = id;
        this.userId = userId;
        this.primaryAddress = primaryAddress;
        this.commercialStatus = commercialStatus;
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
