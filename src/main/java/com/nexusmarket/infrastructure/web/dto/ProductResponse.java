package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import java.util.List;

/**
 * Response body of the product resource.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 *
 * @param id       identifier of the product
 * @param sellerId seller that registered the product
 * @param type     physical or digital product
 * @param variants differences such as colour, size or model
 * @param status   publication status
 */
public record ProductResponse(
        Long id,
        Long sellerId,
        ProductType type,
        List<String> variants,
        ProductStatus status) {
}
