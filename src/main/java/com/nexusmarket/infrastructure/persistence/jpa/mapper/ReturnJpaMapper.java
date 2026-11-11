package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.returns.Return;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ReturnJpaEntity;

/**
 * Explicit mapper between the Return aggregate and its JPA entity.
 *
 * <p>Traceability: OBJ-11.</p>
 */
public final class ReturnJpaMapper {

    private ReturnJpaMapper() {
    }

    public static ReturnJpaEntity toJpa(Return returnRecord) {
        if (returnRecord == null) {
            return null;
        }
        ReturnJpaEntity entity = new ReturnJpaEntity();
        entity.setId(returnRecord.getId());
        entity.setOrderId(returnRecord.getOrderId());
        entity.setOrderItemId(returnRecord.getOrderItemId());
        entity.setBuyerId(returnRecord.getBuyerId());
        entity.setQuantity(returnRecord.getQuantity());
        entity.setReason(returnRecord.getReason());
        entity.setCondition(returnRecord.getCondition());
        entity.setStatus(returnRecord.getStatus());
        entity.setCreatedAt(returnRecord.getCreatedAt());
        entity.setReceivedAt(returnRecord.getReceivedAt());
        return entity;
    }

    public static Return toDomain(ReturnJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Return(entity.getId(), entity.getOrderId(), entity.getOrderItemId(),
                entity.getBuyerId(), entity.getQuantity(), entity.getReason(),
                entity.getCondition(), entity.getStatus(), entity.getCreatedAt(),
                entity.getReceivedAt());
    }
}
