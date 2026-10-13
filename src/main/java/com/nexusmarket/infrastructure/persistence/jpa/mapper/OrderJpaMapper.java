package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.order.Order;
import com.nexusmarket.infrastructure.persistence.jpa.entity.OrderJpaEntity;

/**
 * Explicit mapper between the Order domain aggregate and its JPA entity.
 *
 * <p>Traceability: OBJ-07, OBJ-08.</p>
 */
public final class OrderJpaMapper {

    private OrderJpaMapper() {
    }

    public static OrderJpaEntity toJpa(Order order) {
        if (order == null) {
            return null;
        }
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setBuyerId(order.getBuyerId());
        entity.setStatus(order.getStatus());
        return entity;
    }

    public static Order toDomain(OrderJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        // The Order aggregate exposes no status setter: the domain only allows status
        // changes through changeStatus(...) (RN-13, RN-16). Hydration therefore uses the
        // constructor, which does not bypass those rules.
        return new Order(entity.getId(), entity.getBuyerId(), entity.getStatus());
    }
}
