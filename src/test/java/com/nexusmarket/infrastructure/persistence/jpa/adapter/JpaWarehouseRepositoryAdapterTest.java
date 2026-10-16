package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataWarehouseRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaWarehouseRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.5 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaWarehouseRepositoryAdapter.class)
class JpaWarehouseRepositoryAdapterTest {

    @Autowired
    private JpaWarehouseRepositoryAdapter adapter;

    @Autowired
    private SpringDataWarehouseRepository springDataWarehouseRepository;

    @BeforeEach
    void setUp() {
        springDataWarehouseRepository.deleteAll();
    }

    private Warehouse newSellerWarehouse(Long sellerId) {
        Warehouse warehouse = new Warehouse();
        warehouse.setType(WarehouseType.SELLER);
        warehouse.setSellerId(sellerId);
        return warehouse;
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        Warehouse saved = adapter.save(newSellerWarehouse(100L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSellerId()).isEqualTo(100L);
        assertThat(springDataWarehouseRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void saveKeepsMarketplaceWarehouseWithoutSeller() {
        Warehouse marketplace = new Warehouse();
        marketplace.setType(WarehouseType.MARKETPLACE);

        Warehouse saved = adapter.save(marketplace);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSellerId()).isNull();
        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Warehouse::getType)
                .isEqualTo(WarehouseType.MARKETPLACE);
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        Warehouse saved = adapter.save(newSellerWarehouse(100L));

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Warehouse::getSellerId, Warehouse::getType)
                .containsExactly(100L, WarehouseType.SELLER);
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findBySellerIdReturnsOnlyTheWarehousesOfTheSeller() {
        adapter.save(newSellerWarehouse(100L));
        adapter.save(newSellerWarehouse(100L));
        adapter.save(newSellerWarehouse(200L));

        List<Warehouse> result = adapter.findBySellerId(100L);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(
                warehouse -> assertThat(warehouse.getSellerId()).isEqualTo(100L));
    }

    @Test
    void findBySellerIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findBySellerId(404L)).isEmpty();
    }
}
