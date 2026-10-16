package com.nexusmarket.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code ManageInventoryUseCase.reserveStock}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraints belong to Fase 2.3 and
 * mirror the required fields already checked by
 * {@code com.nexusmarket.application.support.Validation}.</p>
 *
 * @param productId   product whose stock is reserved
 * @param warehouseId warehouse that holds the stock
 * @param amount      quantity to reserve
 */
public record ReserveStockRequest(
        @NotNull Long productId,
        @NotNull Long warehouseId,
        int amount) {
}
