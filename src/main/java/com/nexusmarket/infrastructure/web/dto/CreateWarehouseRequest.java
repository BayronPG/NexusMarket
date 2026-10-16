package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.warehouse.WarehouseType;
import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code ManageWarehousesUseCase.registerWarehouse}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraints belong to Fase 2.3 and
 * mirror the required fields already checked by
 * {@code com.nexusmarket.application.support.Validation}.</p>
 *
 * @param type     classification of the warehouse
 * @param sellerId owner seller
 */
public record CreateWarehouseRequest(
        @NotNull WarehouseType type,
        @NotNull Long sellerId) {
}
