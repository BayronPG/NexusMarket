package com.nexusmarket.application.service;

import com.nexusmarket.application.ActorContext;
import com.nexusmarket.application.Authorization;
import com.nexusmarket.application.Validation;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.port.in.ManageCatalogUseCase;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application service that manages the product catalog.
 *
 * <p>EXPLICIT source: only the Seller registers and administers its own
 * products (responsibilities matrix); products are physical or digital
 * (RN-06).</p>
 *
 * <p>Traceability: OBJ-05, RN-06, RN-07, RN-08, RG-03.</p>
 */
@Service
public class CatalogService implements ManageCatalogUseCase {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;

    public CatalogService(ProductRepository productRepository, SellerRepository sellerRepository) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public Product registerProduct(ActorContext actor, RegisterProductCommand command) {
        Authorization.requireAnyRole(actor, Role.SELLER);
        Validation.requireNotNull(command, "command");
        Validation.requireNotNull(command.type(), "type (physical or digital, RN-06)");
        Validation.requireNotNull(command.status(), "status");

        Seller seller = requireSellerOfActor(actor);

        Product product = new Product();
        product.setSellerId(seller.getId());
        product.setType(command.type());
        product.setStatus(command.status());
        if (command.variants() != null) {
            product.setVariants(new ArrayList<>(command.variants()));
        }
        return productRepository.save(product);
    }

    @Override
    public Product changeProductStatus(ActorContext actor, Long productId, ProductStatus status) {
        Authorization.requireAnyRole(actor, Role.SELLER);
        Validation.requireNotNull(status, "status");

        Seller seller = requireSellerOfActor(actor);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        if (!seller.getId().equals(product.getSellerId())) {
            throw new AuthorizationException("A seller can only manage its own products (RG-03)");
        }

        product.setStatus(status);
        return productRepository.save(product);
    }

    @Override
    public List<Product> findPublishedProducts(ActorContext actor) {
        Authorization.requireAuthenticated(actor);
        return productRepository.findByStatus(ProductStatus.PUBLISHED);
    }

    @Override
    public List<Product> findOwnProducts(ActorContext actor) {
        Authorization.requireAnyRole(actor, Role.SELLER);
        Seller seller = requireSellerOfActor(actor);
        return productRepository.findBySellerId(seller.getId());
    }

    private Seller requireSellerOfActor(ActorContext actor) {
        return sellerRepository.findByUserId(actor.userId())
                .orElseThrow(() -> new NotFoundException("Seller not found for the authenticated user"));
    }
}
