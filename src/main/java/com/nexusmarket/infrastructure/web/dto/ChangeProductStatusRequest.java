package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.catalog.ProductStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code ManageCatalogUseCase.changeProductStatus}.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md; the constraint belongs to Fase 2.3.</p>
 *
 * @param status new publication status of the product
 */
public record ChangeProductStatusRequest(@NotNull ProductStatus status) {
}
