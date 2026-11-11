package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.returns.ReturnCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body to return one line of an order.
 *
 * @param orderId     order identifier
 * @param orderItemId returned line
 * @param quantity    units to return; positive
 * @param reason      motive declared by the buyer
 * @param condition   RESELLABLE or DAMAGED
 */
public record CreateReturnRequest(
        @NotNull Long orderId,
        @NotNull Long orderItemId,
        @Positive int quantity,
        @NotBlank String reason,
        @NotNull ReturnCondition condition) {
}
