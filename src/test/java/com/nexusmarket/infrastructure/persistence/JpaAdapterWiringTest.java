package com.nexusmarket.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaBuyerRepositoryAdapter;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaInventoryRepositoryAdapter;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaOrderRepositoryAdapter;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaProductRepositoryAdapter;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaRefundRepositoryAdapter;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaSellerRepositoryAdapter;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaUserRepositoryAdapter;
import com.nexusmarket.infrastructure.persistence.jpa.adapter.JpaWarehouseRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Wiring verification of the Fase 1.6 substitution: without the "memory"
 * profile, every output port must be served by its JPA adapter.
 *
 * <p>Fase 1.6 of docs/PLAN_API_WEB.md.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class JpaAdapterWiringTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Test
    void everyOutputPortResolvesToItsJpaAdapter() {
        assertThat(userRepository).isInstanceOf(JpaUserRepositoryAdapter.class);
        assertThat(sellerRepository).isInstanceOf(JpaSellerRepositoryAdapter.class);
        assertThat(buyerRepository).isInstanceOf(JpaBuyerRepositoryAdapter.class);
        assertThat(warehouseRepository).isInstanceOf(JpaWarehouseRepositoryAdapter.class);
        assertThat(productRepository).isInstanceOf(JpaProductRepositoryAdapter.class);
        assertThat(inventoryRepository).isInstanceOf(JpaInventoryRepositoryAdapter.class);
        assertThat(orderRepository).isInstanceOf(JpaOrderRepositoryAdapter.class);
        assertThat(refundRepository).isInstanceOf(JpaRefundRepositoryAdapter.class);
    }
}
