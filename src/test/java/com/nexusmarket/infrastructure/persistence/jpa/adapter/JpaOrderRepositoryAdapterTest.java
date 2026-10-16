package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataOrderRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaOrderRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.8 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaOrderRepositoryAdapter.class)
class JpaOrderRepositoryAdapterTest {

    @Autowired
    private JpaOrderRepositoryAdapter adapter;

    @Autowired
    private SpringDataOrderRepository springDataOrderRepository;

    @BeforeEach
    void setUp() {
        springDataOrderRepository.deleteAll();
    }

    private Order newOrder(Long buyerId, OrderStatus status) {
        return new Order(null, buyerId, status);
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        Order saved = adapter.save(newOrder(300L, OrderStatus.CART));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getBuyerId()).isEqualTo(300L);
        assertThat(springDataOrderRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void saveKeepsTheOrderStatus() {
        Order saved = adapter.save(newOrder(300L, OrderStatus.PAID));

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Order::getStatus)
                .isEqualTo(OrderStatus.PAID);
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        Order saved = adapter.save(newOrder(300L, OrderStatus.SHIPPED));

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Order::getBuyerId, Order::getStatus)
                .containsExactly(300L, OrderStatus.SHIPPED);
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findByBuyerIdReturnsTheOrdersOfTheBuyer() {
        adapter.save(newOrder(300L, OrderStatus.CART));
        adapter.save(newOrder(300L, OrderStatus.DELIVERED));
        adapter.save(newOrder(400L, OrderStatus.CART));

        List<Order> result = adapter.findByBuyerId(300L);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(
                order -> assertThat(order.getBuyerId()).isEqualTo(300L));
    }

    @Test
    void findByBuyerIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findByBuyerId(404L)).isEmpty();
    }
}
