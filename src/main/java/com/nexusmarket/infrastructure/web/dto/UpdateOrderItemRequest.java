package com.nexusmarket.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body to change the quantity of a cart line.
 *
 * @param quantity new positive quantity
 */
public record UpdateOrderItemRequest(@NotNull @Positive Integer quantity) {
}
