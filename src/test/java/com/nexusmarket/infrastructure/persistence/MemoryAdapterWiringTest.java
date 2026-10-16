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
import com.nexusmarket.infrastructure.persistence.memory.InMemoryBuyerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryInventoryRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryOrderRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryProductRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryRefundRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemorySellerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryUserRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryWarehouseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Wiring verification of the Fase 1.6 substitution: the in-memory adapters stay
 * available for tests behind the "memory" profile, which deactivates the JPA
 * adapters.
 *
 * <p>Fase 1.6 of docs/PLAN_API_WEB.md.</p>
 */
@SpringBootTest
@ActiveProfiles({"memory", "test"})
class MemoryAdapterWiringTest {

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
    void everyOutputPortResolvesToItsInMemoryAdapter() {
        assertThat(userRepository).isInstanceOf(InMemoryUserRepository.class);
        assertThat(sellerRepository).isInstanceOf(InMemorySellerRepository.class);
        assertThat(buyerRepository).isInstanceOf(InMemoryBuyerRepository.class);
        assertThat(warehouseRepository).isInstanceOf(InMemoryWarehouseRepository.class);
        assertThat(productRepository).isInstanceOf(InMemoryProductRepository.class);
        assertThat(inventoryRepository).isInstanceOf(InMemoryInventoryRepository.class);
        assertThat(orderRepository).isInstanceOf(InMemoryOrderRepository.class);
        assertThat(refundRepository).isInstanceOf(InMemoryRefundRepository.class);
    }
}
