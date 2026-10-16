package com.nexusmarket.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body of {@code RegisterSellerUseCase}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraints belong to Fase 2.3 and
 * mirror the required fields already checked by
 * {@code com.nexusmarket.application.support.Validation}.</p>
 *
 * @param fullName         full name of the seller
 * @param email            email of the seller (RN-15)
 * @param identityDocument identity document of the seller (RN-14)
 */
public record CreateSellerRequest(
        @NotBlank String fullName,
        @NotBlank String email,
        @NotBlank String identityDocument) {
}
