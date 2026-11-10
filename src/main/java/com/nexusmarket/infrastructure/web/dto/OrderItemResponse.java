package com.nexusmarket.infrastructure.web.dto;

import java.math.BigDecimal;

/**
 * Response body of an order line.
 *
 * @param id        identifier of the line
 * @param productId referenced product
 * @param quantity  number of units
 * @param unitPrice price snapshot when the product was added
 * @param subtotal  {@code quantity * unitPrice}
 */
public record OrderItemResponse(
        Long id,
        Long productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
