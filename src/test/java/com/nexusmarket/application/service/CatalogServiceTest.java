package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.ManageCatalogUseCase.RegisterProductCommand;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryProductRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemorySellerRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the catalog management use case.
 *
 * <p>Traceability: OBJ-05, RN-06 (physical or digital), responsibilities
 * matrix (only the Seller registers products), RG-03.</p>
 */
class CatalogServiceTest {

    private InMemoryProductRepository products;
    private InMemorySellerRepository sellers;
    private CatalogService service;

    private static final long SELLER_USER_ID = 10L;
    private static final long OTHER_SELLER_USER_ID = 20L;

    private final ActorContext seller = new ActorContext(SELLER_USER_ID, Role.SELLER);
    private final ActorContext otherSeller = new ActorContext(OTHER_SELLER_USER_ID, Role.SELLER);
    private final ActorContext buyer = new ActorContext(30L, Role.BUYER);

    @BeforeEach
    void setUp() {
        products = new InMemoryProductRepository();
        sellers = new InMemorySellerRepository();
        service = new CatalogService(products, sellers);

        Seller linkedSeller = new Seller();
        linkedSeller.setUserId(SELLER_USER_ID);
        sellers.save(linkedSeller);

        Seller anotherSeller = new Seller();
        anotherSeller.setUserId(OTHER_SELLER_USER_ID);
        sellers.save(anotherSeller);
    }

    @Test
    void sellerRegistersItsProduct() {
        Product product = service.registerProduct(seller,
                new RegisterProductCommand(ProductType.PHYSICAL, ProductStatus.PUBLISHED, List.of("blue", "M")));

        assertThat(product.getId()).isNotNull();
        assertThat(product.getType()).isEqualTo(ProductType.PHYSICAL);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
        assertThat(product.getVariants()).containsExactly("blue", "M");
        assertThat(product.getSellerId()).isNotNull();
    }

    @Test
    void onlySellersRegisterProducts() {
        assertThatThrownBy(() -> service.registerProduct(buyer,
                new RegisterProductCommand(ProductType.DIGITAL, ProductStatus.PUBLISHED, null)))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void productTypeIsRequired() {
        assertThatThrownBy(() -> service.registerProduct(seller,
                new RegisterProductCommand(null, ProductStatus.PUBLISHED, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void sellerWithoutSellerRecordCannotRegisterProducts() {
        ActorContext unknownSeller = new ActorContext(999L, Role.SELLER);

        assertThatThrownBy(() -> service.registerProduct(unknownSeller,
                new RegisterProductCommand(ProductType.DIGITAL, ProductStatus.PUBLISHED, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void sellerChangesTheStatusOfItsOwnProduct() {
        Product product = service.registerProduct(seller,
                new RegisterProductCommand(ProductType.PHYSICAL, ProductStatus.SUSPENDED, null));

        Product updated = service.changeProductStatus(seller, product.getId(), ProductStatus.PUBLISHED);

        assertThat(updated.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
    }

    @Test
    void sellerCannotChangeTheStatusOfAnotherSellersProduct() {
        Product product = service.registerProduct(otherSeller,
                new RegisterProductCommand(ProductType.PHYSICAL, ProductStatus.PUBLISHED, null));

        assertThatThrownBy(() -> service.changeProductStatus(seller, product.getId(), ProductStatus.SUSPENDED))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void publishedCatalogOnlyContainsPublishedProducts() {
        service.registerProduct(seller,
                new RegisterProductCommand(ProductType.PHYSICAL, ProductStatus.PUBLISHED, null));
        service.registerProduct(seller,
                new RegisterProductCommand(ProductType.DIGITAL, ProductStatus.SUSPENDED, null));

        assertThat(service.findPublishedProducts(buyer, null, PageQuery.of(0, 20, "id,asc")).content())
                .extracting(Product::getStatus)
                .containsOnly(ProductStatus.PUBLISHED);
    }

    @Test
    void sellerListsItsOwnProducts() {
        service.registerProduct(seller,
                new RegisterProductCommand(ProductType.PHYSICAL, ProductStatus.PUBLISHED, null));
        service.registerProduct(otherSeller,
                new RegisterProductCommand(ProductType.DIGITAL, ProductStatus.PUBLISHED, null));

        assertThat(service.findOwnProducts(seller, null, PageQuery.of(0, 20, "id,asc")).content()).hasSize(1);
    }
}
