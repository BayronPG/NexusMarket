package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.infrastructure.persistence.jpa.entity.RefundJpaEntity;

/**
 * Explicit mapper between the Refund domain aggregate and its JPA entity.
 *
 * <p>Traceability: OBJ-11.</p>
 */
public final class RefundJpaMapper {

    private RefundJpaMapper() {
    }

    public static RefundJpaEntity toJpa(Refund refund) {
        if (refund == null) {
            return null;
        }
        RefundJpaEntity entity = new RefundJpaEntity();
        entity.setId(refund.getId());
        entity.setOrderId(refund.getOrderId());
        return entity;
    }

    public static Refund toDomain(RefundJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Refund refund = new Refund();
        refund.setId(entity.getId());
        refund.setOrderId(entity.getOrderId());
        return refund;
    }
}
