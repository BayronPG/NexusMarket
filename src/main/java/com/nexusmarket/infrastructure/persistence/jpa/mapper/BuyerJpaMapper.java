package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.infrastructure.persistence.jpa.entity.BuyerJpaEntity;
import java.util.ArrayList;

/**
 * Explicit mapper between the Buyer domain aggregate and its JPA entity.
 *
 * <p>The {@code additionalAddresses} list is copied defensively so the domain
 * and the entity do not share mutable state. Traceability: OBJ-03.</p>
 */
public final class BuyerJpaMapper {

    private BuyerJpaMapper() {
    }

    public static BuyerJpaEntity toJpa(Buyer buyer) {
        if (buyer == null) {
            return null;
        }
        BuyerJpaEntity entity = new BuyerJpaEntity();
        entity.setId(buyer.getId());
        entity.setUserId(buyer.getUserId());
        entity.setPrimaryAddress(buyer.getPrimaryAddress());
        entity.setAdditionalAddresses(buyer.getAdditionalAddresses() == null
                ? new ArrayList<>()
                : new ArrayList<>(buyer.getAdditionalAddresses()));
        entity.setCommercialStatus(buyer.getCommercialStatus());
        return entity;
    }

    public static Buyer toDomain(BuyerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Buyer buyer = new Buyer();
        buyer.setId(entity.getId());
        buyer.setUserId(entity.getUserId());
        buyer.setPrimaryAddress(entity.getPrimaryAddress());
        buyer.setAdditionalAddresses(entity.getAdditionalAddresses() == null
                ? new ArrayList<>()
                : new ArrayList<>(entity.getAdditionalAddresses()));
        buyer.setCommercialStatus(entity.getCommercialStatus());
        return buyer;
    }
}
