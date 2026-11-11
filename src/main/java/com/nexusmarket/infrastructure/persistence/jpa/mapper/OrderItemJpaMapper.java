package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.infrastructure.persistence.jpa.entity.OrderItemJpaEntity;

/**
 * Explicit mapper between the OrderItem aggregate and its JPA entity.
 *
 * <p>Traceability: OBJ-07, OBJ-08, OBJ-10.</p>
 */
public final class OrderItemJpaMapper {

    private OrderItemJpaMapper() {
    }

    public static OrderItemJpaEntity toJpa(OrderItem item) {
        if (item == null) {
            return null;
        }
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setId(item.getId());
        entity.setOrderId(item.getOrderId());
        entity.setProductId(item.getProductId());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setFulfillmentStatus(item.getStatus());
        return entity;
    }

    public static OrderItem toDomain(OrderItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new OrderItem(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getFulfillmentStatus());
    }
}
