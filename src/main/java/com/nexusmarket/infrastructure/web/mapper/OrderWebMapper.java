package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.infrastructure.web.dto.OrderItemResponse;
import com.nexusmarket.infrastructure.web.dto.OrderResponse;
import java.util.List;

/**
 * Explicit mapper between the order web DTOs and the domain types.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md. The total is calculated by the domain
 * aggregate; the mapper only formats the lines.</p>
 */
public final class OrderWebMapper {

    private OrderWebMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderWebMapper::toItemResponse)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                order.getStatus(),
                items,
                order.total());
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.subtotal());
    }
}
