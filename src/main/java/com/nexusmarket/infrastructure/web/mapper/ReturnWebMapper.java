package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.application.port.in.ManageReturnsUseCase.RequestReturnCommand;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.returns.Return;
import com.nexusmarket.infrastructure.web.dto.CreateReturnRequest;
import com.nexusmarket.infrastructure.web.dto.ReturnResponse;

/**
 * Explicit mapper between the return web DTOs and the application types.
 *
 * <p>Traceability: OBJ-11.</p>
 */
public final class ReturnWebMapper {

    private ReturnWebMapper() {
    }

    public static RequestReturnCommand toCommand(CreateReturnRequest request) {
        if (request == null) {
            return null;
        }
        return new RequestReturnCommand(request.orderId(), request.orderItemId(),
                request.quantity(), request.reason(), request.condition());
    }

    public static ReturnResponse toResponse(Return returnRecord, Refund refund) {
        if (returnRecord == null) {
            return null;
        }
        Long refundId = refund == null ? returnRecord.getRefundId() : refund.getId();
        return new ReturnResponse(returnRecord.getId(), returnRecord.getOrderId(),
                returnRecord.getOrderItemId(), returnRecord.getBuyerId(),
                returnRecord.getQuantity(), returnRecord.getReason(), returnRecord.getCondition(),
                returnRecord.getStatus(), returnRecord.getCreatedAt(),
                returnRecord.getReceivedAt(), refundId);
    }
}
