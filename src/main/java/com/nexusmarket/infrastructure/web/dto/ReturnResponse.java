package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.returns.ReturnCondition;
import com.nexusmarket.domain.returns.ReturnStatus;
import java.time.LocalDateTime;

/**
 * Response body of the return resource.
 *
 * @param id          identifier of the return
 * @param orderId     order that contains the returned line
 * @param orderItemId returned line
 * @param buyerId     buyer that owns the order
 * @param quantity    returned units
 * @param reason      motive declared by the buyer
 * @param condition   RESELLABLE or DAMAGED
 * @param status      lifecycle state
 * @param createdAt   creation timestamp
 * @param receivedAt  reception timestamp; null until received
 * @param refundId    generated refund; null until the return is received
 */
public record ReturnResponse(
        Long id,
        Long orderId,
        Long orderItemId,
        Long buyerId,
        int quantity,
        String reason,
        ReturnCondition condition,
        ReturnStatus status,
        LocalDateTime createdAt,
        LocalDateTime receivedAt,
        Long refundId) {
}
