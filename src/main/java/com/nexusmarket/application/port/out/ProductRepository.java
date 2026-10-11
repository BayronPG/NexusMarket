package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import java.util.List;
import java.util.Optional;

/**
 * Output port for product persistence.
 *
 * <p>Traceability: OBJ-05.</p>
 */
public interface ProductRepository {

    /** Stores the product, assigning an identifier when it is new. */
    Product save(Product product);

    /** Finds a product by its identifier. */
    Optional<Product> findById(Long id);

    /** Lists the products registered by a seller. */
    List<Product> findBySellerId(Long sellerId);

    /** Lists the products that are visible in the public catalog (status PUBLISHED). */
    List<Product> findByStatus(ProductStatus status);

    /** Lists every stored product. */
    List<Product> findAll();
}
