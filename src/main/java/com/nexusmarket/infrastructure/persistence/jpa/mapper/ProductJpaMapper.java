package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import java.util.ArrayList;

/**
 * Explicit mapper between the Product domain aggregate and its JPA entity.
 *
 * <p>The {@code variants} list is copied defensively so the domain and the
 * entity do not share mutable state. Traceability: OBJ-05.</p>
 */
public final class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static ProductJpaEntity toJpa(Product product) {
        if (product == null) {
            return null;
        }
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId());
        entity.setSellerId(product.getSellerId());
        entity.setType(product.getType());
        entity.setVariants(product.getVariants() == null
                ? new ArrayList<>()
                : new ArrayList<>(product.getVariants()));
        entity.setStatus(product.getStatus());
        return entity;
    }

    public static Product toDomain(ProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Product product = new Product();
        product.setId(entity.getId());
        product.setSellerId(entity.getSellerId());
        product.setType(entity.getType());
        product.setVariants(entity.getVariants() == null
                ? new ArrayList<>()
                : new ArrayList<>(entity.getVariants()));
        product.setStatus(entity.getStatus());
        return product;
    }
}
