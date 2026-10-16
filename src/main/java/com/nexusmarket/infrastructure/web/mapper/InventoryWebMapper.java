package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.application.port.in.ManageInventoryUseCase.RegisterStockCommand;
import com.nexusmarket.application.port.in.ManageInventoryUseCase.ReserveStockCommand;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.infrastructure.web.dto.InventoryResponse;
import com.nexusmarket.infrastructure.web.dto.RegisterStockRequest;
import com.nexusmarket.infrastructure.web.dto.ReserveStockRequest;
import java.util.List;

/**
 * Explicit mapper between the inventory web DTOs and the domain/application types.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 */
public final class InventoryWebMapper {

    private InventoryWebMapper() {
    }

    public static RegisterStockCommand toRegisterStockCommand(RegisterStockRequest request) {
        if (request == null) {
            return null;
        }
        return new RegisterStockCommand(request.productId(), request.warehouseId(), request.quantity());
    }

    public static ReserveStockCommand toReserveStockCommand(ReserveStockRequest request) {
        if (request == null) {
            return null;
        }
        return new ReserveStockCommand(request.productId(), request.warehouseId(), request.amount());
    }

    public static InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getWarehouseId(),
                inventory.getQuantity(),
                inventory.getDamagedQuantity());
    }

    public static List<InventoryResponse> toResponses(List<Inventory> inventories) {
        return inventories.stream().map(InventoryWebMapper::toResponse).toList();
    }
}
