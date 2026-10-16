package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code ManageOrdersUseCase.changeOrderStatus}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraint belongs to Fase 2.3.</p>
 *
 * @param status next status of the order lifecycle (RN-16)
 */
public record ChangeOrderStatusRequest(@NotNull OrderStatus status) {
}
