package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataOrderItemRepository;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataOrderRepository;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaOrderItemRepositoryAdapter} against the embedded H2
 * database and the V3 Flyway migration.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaOrderItemRepositoryAdapter.class)
class JpaOrderItemRepositoryAdapterTest {

    @Autowired
    private JpaOrderItemRepositoryAdapter adapter;

    @Autowired
    private SpringDataOrderItemRepository springDataOrderItemRepository;

    @Autowired
    private SpringDataOrderRepository springDataOrderRepository;

    @Autowired
    private SpringDataProductRepository springDataProductRepository;

    private Long orderId;
    private Long productId;

    @BeforeEach
    void setUp() {
        springDataOrderItemRepository.deleteAll();
        springDataOrderRepository.deleteAll();
        springDataProductRepository.deleteAll();

        OrderJpaEntity order = new OrderJpaEntity();
        order.setBuyerId(300L);
        order.setStatus(com.nexusmarket.domain.order.OrderStatus.CART);
        orderId = springDataOrderRepository.save(order).getId();

        ProductJpaEntity product = new ProductJpaEntity();
        product.setSellerId(400L);
        product.setType(ProductType.PHYSICAL);
        product.setStatus(ProductStatus.PUBLISHED);
        product.setPrice(new BigDecimal("12.50"));
        productId = springDataProductRepository.save(product).getId();
    }

    private OrderItem item(int quantity) {
        return new OrderItem(null, orderId, productId, quantity, new BigDecimal("12.50"));
    }

    @Test
    void saveAssignsIdAndReturnsTheDomain() {
        OrderItem saved = adapter.save(item(2));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(saved.subtotal()).isEqualByComparingTo("25.00");
    }

    @Test
    void findByOrderIdReturnsTheLinesOrderedById() {
        adapter.save(item(1));
        adapter.save(new OrderItem(null, orderId, otherProduct(), 3, new BigDecimal("5.00")));

        var result = adapter.findByOrderId(orderId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isLessThan(result.get(1).getId());
    }

    @Test
    void deleteByIdRemovesTheLine() {
        OrderItem saved = adapter.save(item(1));

        adapter.deleteById(saved.getId());

        assertThat(adapter.findById(saved.getId())).isEmpty();
    }

    @Test
    void theDatabaseRejectsADuplicatedProductInTheSameOrder() {
        adapter.save(item(1));

        assertThatThrownBy(() -> adapter.save(item(2)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long otherProduct() {
        ProductJpaEntity product = new ProductJpaEntity();
        product.setSellerId(400L);
        product.setType(ProductType.DIGITAL);
        product.setStatus(ProductStatus.PUBLISHED);
        product.setPrice(new BigDecimal("5.00"));
        return springDataProductRepository.save(product).getId();
    }
}
