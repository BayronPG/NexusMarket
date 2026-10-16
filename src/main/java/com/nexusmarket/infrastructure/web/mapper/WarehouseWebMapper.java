package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.application.port.in.ManageWarehousesUseCase.RegisterCommand;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.infrastructure.web.dto.CreateWarehouseRequest;
import com.nexusmarket.infrastructure.web.dto.WarehouseResponse;
import java.util.List;

/**
 * Explicit mapper between the warehouse web DTOs and the domain/application types.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 */
public final class WarehouseWebMapper {

    private WarehouseWebMapper() {
    }

    public static RegisterCommand toCommand(CreateWarehouseRequest request) {
        if (request == null) {
            return null;
        }
        return new RegisterCommand(request.type(), request.sellerId());
    }

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        return new WarehouseResponse(warehouse.getId(), warehouse.getType(), warehouse.getSellerId());
    }

    public static List<WarehouseResponse> toResponses(List<Warehouse> warehouses) {
        return warehouses.stream().map(WarehouseWebMapper::toResponse).toList();
    }
}
