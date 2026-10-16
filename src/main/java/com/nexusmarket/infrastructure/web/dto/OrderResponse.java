package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.order.OrderStatus;

/**
 * Response body of the order resource.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 *
 * @param id      identifier of the order
 * @param buyerId buyer that made the order
 * @param status  current lifecycle status
 */
public record OrderResponse(Long id, Long buyerId, OrderStatus status) {
}
