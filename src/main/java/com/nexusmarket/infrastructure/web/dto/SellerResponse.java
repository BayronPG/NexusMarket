package com.nexusmarket.infrastructure.web.dto;

/**
 * Response body of the seller resource.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 *
 * @param id     identifier of the seller
 * @param userId identifier of the user that holds the seller role
 */
public record SellerResponse(Long id, Long userId) {
}
