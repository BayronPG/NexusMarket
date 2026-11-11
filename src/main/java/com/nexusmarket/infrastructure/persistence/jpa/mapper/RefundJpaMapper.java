package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.returns.RefundStatus;
import com.nexusmarket.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import java.math.BigDecimal;

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
        entity.setReturnId(refund.getReturnId());
        entity.setOrderId(refund.getOrderId());
        entity.setOrderItemId(refund.getOrderItemId());
        entity.setAmount(refund.getAmount());
        entity.setStatus(refund.getStatus());
        entity.setCreatedAt(refund.getCreatedAt());
        return entity;
    }

    public static Refund toDomain(RefundJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        // Rows created before V5 have no amount or status; they remain readable.
        BigDecimal amount = entity.getAmount() == null ? BigDecimal.ZERO : entity.getAmount();
        RefundStatus status = entity.getStatus() == null
                ? RefundStatus.REGISTERED : entity.getStatus();
        return new Refund(entity.getId(), entity.getReturnId(), entity.getOrderId(),
                entity.getOrderItemId(), amount, status, entity.getCreatedAt());
    }
}
