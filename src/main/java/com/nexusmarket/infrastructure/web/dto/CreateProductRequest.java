package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request body of {@code ManageCatalogUseCase.registerProduct}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraints belong to Fase 2.3 and
 * mirror the required fields already checked by
 * {@code com.nexusmarket.application.support.Validation}.</p>
 *
 * @param type     physical or digital product
 * @param status   publication status
 * @param variants differences such as colour, size or model
 */
public record CreateProductRequest(
        @NotNull ProductType type,
        @NotNull ProductStatus status,
        List<String> variants) {
}
