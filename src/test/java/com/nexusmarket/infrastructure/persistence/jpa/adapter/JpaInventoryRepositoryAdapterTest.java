package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataInventoryRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaInventoryRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.7 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaInventoryRepositoryAdapter.class)
class JpaInventoryRepositoryAdapterTest {

    @Autowired
    private JpaInventoryRepositoryAdapter adapter;

    @Autowired
    private SpringDataInventoryRepository springDataInventoryRepository;

    @BeforeEach
    void setUp() {
        springDataInventoryRepository.deleteAll();
    }

    private Inventory newInventory(Long productId, Long warehouseId, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setWarehouseId(warehouseId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        Inventory saved = adapter.save(newInventory(1L, 10L, 5));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(5);
        assertThat(springDataInventoryRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        Inventory saved = adapter.save(newInventory(1L, 10L, 5));

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Inventory::getProductId, Inventory::getWarehouseId, Inventory::getQuantity)
                .containsExactly(1L, 10L, 5);
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findByProductIdAndWarehouseIdReturnsThatStock() {
        adapter.save(newInventory(1L, 10L, 5));
        adapter.save(newInventory(1L, 20L, 7));

        assertThat(adapter.findByProductIdAndWarehouseId(1L, 10L))
                .isPresent()
                .get()
                .extracting(Inventory::getQuantity)
                .isEqualTo(5);
    }

    @Test
    void findByProductIdAndWarehouseIdReturnsEmptyWhenAbsent() {
        adapter.save(newInventory(1L, 10L, 5));

        assertThat(adapter.findByProductIdAndWarehouseId(1L, 99L)).isEmpty();
    }

    @Test
    void findByProductIdReturnsTheDistributedStock() {
        adapter.save(newInventory(1L, 10L, 5));
        adapter.save(newInventory(1L, 20L, 7));
        adapter.save(newInventory(2L, 10L, 3));

        List<Inventory> result = adapter.findByProductId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(
                inventory -> assertThat(inventory.getProductId()).isEqualTo(1L));
    }

    @Test
    void findAllReturnsEveryStoredRecord() {
        adapter.save(newInventory(1L, 10L, 5));
        adapter.save(newInventory(2L, 10L, 3));

        assertThat(adapter.findAll()).hasSize(2);
    }
}
