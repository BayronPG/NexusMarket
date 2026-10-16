package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.application.port.in.ManageRefundsUseCase.RequestRefundCommand;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.infrastructure.web.dto.CreateRefundRequest;
import com.nexusmarket.infrastructure.web.dto.RefundResponse;

/**
 * Explicit mapper between the refund web DTOs and the domain/application types.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 */
public final class RefundWebMapper {

    private RefundWebMapper() {
    }

    public static RequestRefundCommand toCommand(CreateRefundRequest request) {
        if (request == null) {
            return null;
        }
        return new RequestRefundCommand(request.orderId());
    }

    public static RefundResponse toResponse(Refund refund) {
        if (refund == null) {
            return null;
        }
        return new RefundResponse(refund.getId(), refund.getOrderId());
    }
}
