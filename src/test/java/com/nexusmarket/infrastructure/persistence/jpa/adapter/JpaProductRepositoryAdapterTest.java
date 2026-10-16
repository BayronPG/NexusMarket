package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataProductRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaProductRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.6 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaProductRepositoryAdapter.class)
class JpaProductRepositoryAdapterTest {

    @Autowired
    private JpaProductRepositoryAdapter adapter;

    @Autowired
    private SpringDataProductRepository springDataProductRepository;

    @BeforeEach
    void setUp() {
        springDataProductRepository.deleteAll();
    }

    private Product newProduct(Long sellerId, ProductStatus status) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setType(ProductType.PHYSICAL);
        product.setVariants(List.of("Color: negro"));
        product.setStatus(status);
        return product;
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        Product saved = adapter.save(newProduct(100L, ProductStatus.PUBLISHED));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSellerId()).isEqualTo(100L);
        assertThat(springDataProductRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void savePersistsTheVariantsList() {
        Product saved = adapter.save(newProduct(100L, ProductStatus.PUBLISHED));

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Product::getVariants)
                .isEqualTo(List.of("Color: negro"));
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        Product saved = adapter.save(newProduct(100L, ProductStatus.PUBLISHED));

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Product::getType, Product::getStatus)
                .containsExactly(ProductType.PHYSICAL, ProductStatus.PUBLISHED);
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findBySellerIdReturnsOnlyTheProductsOfTheSeller() {
        adapter.save(newProduct(100L, ProductStatus.PUBLISHED));
        adapter.save(newProduct(100L, ProductStatus.SUSPENDED));
        adapter.save(newProduct(200L, ProductStatus.PUBLISHED));

        List<Product> result = adapter.findBySellerId(100L);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(
                product -> assertThat(product.getSellerId()).isEqualTo(100L));
    }

    @Test
    void findByStatusReturnsOnlyProductsWithThatStatus() {
        adapter.save(newProduct(100L, ProductStatus.PUBLISHED));
        adapter.save(newProduct(100L, ProductStatus.SUSPENDED));
        adapter.save(newProduct(200L, ProductStatus.PUBLISHED));

        List<Product> result = adapter.findByStatus(ProductStatus.PUBLISHED);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(
                product -> assertThat(product.getStatus()).isEqualTo(ProductStatus.PUBLISHED));
    }

    @Test
    void findAllReturnsEveryStoredProduct() {
        adapter.save(newProduct(100L, ProductStatus.PUBLISHED));
        adapter.save(newProduct(200L, ProductStatus.DISCONTINUED));

        assertThat(adapter.findAll()).hasSize(2);
    }

    @Test
    void findBySellerIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findBySellerId(404L)).isEmpty();
    }
}
