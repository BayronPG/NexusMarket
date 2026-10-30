package com.nexusmarket.application.port.in;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import java.util.List;

/**
 * Inbound port: management of the product catalog.
 *
 * <p>EXPLICIT source: registering products is a responsibility of the Seller
 * (responsibilities matrix); the seller registers and administers "sus
 * productos" (OBJ-05). Products are physical or digital (RN-06).</p>
 *
 * <p>Traceability: OBJ-05, RN-06, RN-07, RN-08, RG-03.</p>
 */
public interface ManageCatalogUseCase {

    /**
     * Input data of a product registration.
     *
     * @param type     required; physical or digital (RN-06)
     * @param status   required publication status
     * @param variants optional differences such as color, size or model
     */
    record RegisterProductCommand(ProductType type, ProductStatus status, List<String> variants) {
    }

    /**
     * Registers a product owned by the seller identified by the actor.
     *
     * @param actor   authenticated actor; must hold the SELLER role
     * @param command input data
     * @return the created product
     */
    Product registerProduct(ActorContext actor, RegisterProductCommand command);

    /**
     * Changes the publication status of a product owned by the actor.
     *
     * @param actor     authenticated actor; must hold the SELLER role
     * @param productId product identifier
     * @param status    new status
     * @return the updated product
     */
    Product changeProductStatus(ActorContext actor, Long productId, ProductStatus status);

    /**
     * Lists the products visible in the public catalog.
     *
     * @param actor    authenticated actor
     * @param sellerId optional seller filter; null means every seller
     * @param query    pagination and sort
     * @return one page of published products
     */
    PageResult<Product> findPublishedProducts(ActorContext actor, Long sellerId, PageQuery query);

    /**
     * Lists the products registered by the actor.
     *
     * @param actor  authenticated actor; must hold the SELLER role
     * @param status optional status filter; null means every status
     * @param query  pagination and sort
     * @return one page of the seller products
     */
    PageResult<Product> findOwnProducts(ActorContext actor, ProductStatus status, PageQuery query);
}
