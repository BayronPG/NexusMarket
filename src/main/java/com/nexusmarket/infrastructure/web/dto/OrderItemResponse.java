package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.order.OrderItemStatus;
import java.math.BigDecimal;

/**
 * Response body of an order line.
 *
 * @param id        identifier of the line
 * @param productId referenced product
 * @param sellerId  seller that owns the product; derived from Product
 * @param quantity  number of units
 * @param unitPrice price snapshot when the product was added
 * @param subtotal  {@code quantity * unitPrice}
 * @param status    fulfillment state of the line
 */
public record OrderItemResponse(
        Long id,
        Long productId,
        Long sellerId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        OrderItemStatus status) {
}
