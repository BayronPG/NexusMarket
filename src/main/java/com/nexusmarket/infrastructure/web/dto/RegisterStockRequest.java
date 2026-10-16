package com.nexusmarket.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code ManageInventoryUseCase.registerStock}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraints belong to Fase 2.3 and
 * mirror the required fields already checked by
 * {@code com.nexusmarket.application.support.Validation}.</p>
 *
 * @param productId   product the stock belongs to (RN-09)
 * @param warehouseId warehouse that holds the stock (RN-09)
 * @param quantity    quantity to register
 */
public record RegisterStockRequest(
        @NotNull Long productId,
        @NotNull Long warehouseId,
        int quantity) {
}
