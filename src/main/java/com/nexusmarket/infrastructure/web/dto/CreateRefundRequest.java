package com.nexusmarket.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code ManageRefundsUseCase.requestRefund}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraint belongs to Fase 2.3.</p>
 *
 * @param orderId order being refunded
 */
public record CreateRefundRequest(@NotNull Long orderId) {
}
