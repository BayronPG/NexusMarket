package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response body of the order resource, including its lines and total.
 *
 * @param id      identifier of the order
 * @param buyerId buyer that made the order
 * @param status  current lifecycle status
 * @param items   lines of the order
 * @param total   sum of the line subtotals
 */
public record OrderResponse(
        Long id,
        Long buyerId,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal total) {
}
