package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.infrastructure.persistence.jpa.entity.SellerJpaEntity;

/**
 * Explicit mapper between the Seller domain aggregate and its JPA entity.
 *
 * <p>Traceability: OBJ-02.</p>
 */
public final class SellerJpaMapper {

    private SellerJpaMapper() {
    }

    public static SellerJpaEntity toJpa(Seller seller) {
        if (seller == null) {
            return null;
        }
        SellerJpaEntity entity = new SellerJpaEntity();
        entity.setId(seller.getId());
        entity.setUserId(seller.getUserId());
        return entity;
    }

    public static Seller toDomain(SellerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Seller seller = new Seller();
        seller.setId(entity.getId());
        seller.setUserId(entity.getUserId());
        return seller;
    }
}
