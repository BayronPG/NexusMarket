package com.nexusmarket.application.port.out;

import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
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

    /** Lists the products registered by a seller, optionally filtered by status. */
    PageResult<Product> findBySellerId(Long sellerId, ProductStatus status, PageQuery query);

    /** Lists the products visible in the public catalog, optionally filtered by seller. */
    PageResult<Product> findByStatus(ProductStatus status, Long sellerId, PageQuery query);

    /** Lists every stored product. */
    List<Product> findAll();
}
