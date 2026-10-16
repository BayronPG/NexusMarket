package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.application.port.in.ManageCatalogUseCase.RegisterProductCommand;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.infrastructure.web.dto.CreateProductRequest;
import com.nexusmarket.infrastructure.web.dto.ProductResponse;
import java.util.List;

/**
 * Explicit mapper between the catalog web DTOs and the domain/application types.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md. The domain entity is never exposed
 * directly: every response is converted into a {@link ProductResponse}.</p>
 */
public final class ProductWebMapper {

    private ProductWebMapper() {
    }

    public static RegisterProductCommand toCommand(CreateProductRequest request) {
        if (request == null) {
            return null;
        }
        return new RegisterProductCommand(request.type(), request.status(), request.variants());
    }

    public static ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                product.getType(),
                product.getVariants(),
                product.getStatus());
    }

    public static List<ProductResponse> toResponses(List<Product> products) {
        return products.stream().map(ProductWebMapper::toResponse).toList();
    }
}
