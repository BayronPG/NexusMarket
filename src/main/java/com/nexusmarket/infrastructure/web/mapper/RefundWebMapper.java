package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.infrastructure.web.dto.RefundResponse;

/**
 * Explicit mapper between the refund domain type and its response DTO.
 *
 * <p>Traceability: OBJ-11.</p>
 */
public final class RefundWebMapper {

    private RefundWebMapper() {
    }

    public static RefundResponse toResponse(Refund refund) {
        if (refund == null) {
            return null;
        }
        return new RefundResponse(refund.getId(), refund.getReturnId(), refund.getOrderId(),
                refund.getOrderItemId(), refund.getAmount(), refund.getStatus(),
                refund.getCreatedAt());
    }
}
