package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.warehouse.WarehouseType;

/**
 * Response body of the warehouse resource.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 *
 * @param id       identifier of the warehouse
 * @param type     classification of the warehouse
 * @param sellerId owner seller; null for MARKETPLACE warehouses
 */
public record WarehouseResponse(Long id, WarehouseType type, Long sellerId) {
}
