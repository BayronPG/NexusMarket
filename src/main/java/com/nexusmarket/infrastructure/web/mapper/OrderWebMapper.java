package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.domain.order.Order;
import com.nexusmarket.infrastructure.web.dto.OrderResponse;

/**
 * Explicit mapper between the order web DTOs and the domain types.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 */
public final class OrderWebMapper {

    private OrderWebMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderResponse(order.getId(), order.getBuyerId(), order.getStatus());
    }
}
