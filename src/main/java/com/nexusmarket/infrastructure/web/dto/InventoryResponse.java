package com.nexusmarket.infrastructure.web.dto;

/**
 * Response body of the inventory resource.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 *
 * @param id              identifier of the inventory record
 * @param productId       product the stock belongs to
 * @param warehouseId     warehouse that holds the stock
 * @param quantity        available stock
 * @param damagedQuantity stock marked as damaged
 */
public record InventoryResponse(
        Long id,
        Long productId,
        Long warehouseId,
        int quantity,
        int damagedQuantity) {
}
