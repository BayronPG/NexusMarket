package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.warehouse.WarehouseType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code ManageWarehousesUseCase.registerWarehouse}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraints belong to Fase 2.3 and
 * mirror the required fields already checked by
 * {@code com.nexusmarket.application.support.Validation}.</p>
 *
 * <p>The conditional rule mirrors {@code WarehouseService}: a SELLER warehouse
 * requires an owner, while a MARKETPLACE warehouse must not declare one.</p>
 *
 * @param type     classification of the warehouse
 * @param sellerId owner seller; required for SELLER and null for MARKETPLACE
 */
public record CreateWarehouseRequest(
        @NotNull WarehouseType type,
        Long sellerId) {

    @AssertTrue(message = "sellerId is required for a SELLER warehouse and must be null for a MARKETPLACE warehouse")
    public boolean isSellerIdConsistentWithType() {
        if (type == null) {
            return true;
        }
        return type == WarehouseType.SELLER ? sellerId != null : sellerId == null;
    }
}
