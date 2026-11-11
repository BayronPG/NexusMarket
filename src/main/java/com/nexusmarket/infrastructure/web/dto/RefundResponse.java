package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.returns.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response body of the refund resource.
 *
 * @param id          identifier of the refund
 * @param returnId    return that generated it
 * @param orderId     order that contains the refunded line
 * @param orderItemId refunded line
 * @param amount      returned quantity multiplied by the historical unit price
 * @param status      refund state
 * @param createdAt   creation timestamp
 */
public record RefundResponse(
        Long id,
        Long returnId,
        Long orderId,
        Long orderItemId,
        BigDecimal amount,
        RefundStatus status,
        LocalDateTime createdAt) {
}
