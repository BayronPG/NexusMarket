package com.nexusmarket.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body to add a product to a cart.
 *
 * @param productId product identifier
 * @param quantity  positive quantity
 */
public record AddOrderItemRequest(
        @NotNull @Positive Long productId,
        @NotNull @Positive Integer quantity) {
}
