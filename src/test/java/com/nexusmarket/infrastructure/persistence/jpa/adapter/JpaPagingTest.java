package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Pagination, sort and filters of the JPA adapters against H2.
 *
 * <p>Fase 2.9 of docs/PLAN_API_WEB.md. Each test uses its own seller so the
 * shared context does not interfere.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class JpaPagingTest {

    @Autowired
    private ProductRepository products;

    @Autowired
    private WarehouseRepository warehouses;

    @Autowired
    private InventoryRepository inventories;

    @Test
    void productsArePagedAndSortedByTheRequestedField() {
        for (int i = 0; i < 5; i++) {
            products.save(product(7101L, ProductStatus.PUBLISHED));
        }

        PageResult<Product> page = products.findByStatus(ProductStatus.PUBLISHED, 7101L,
                new PageQuery(1, 2, "id,desc"));

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.content().get(0).getId()).isGreaterThan(page.content().get(1).getId());
    }

    @Test
    void productsAreFilteredByStatusAndSeller() {
        products.save(product(7102L, ProductStatus.PUBLISHED));
        products.save(product(7102L, ProductStatus.PUBLISHED));
        products.save(product(7102L, ProductStatus.SUSPENDED));
        products.save(product(7103L, ProductStatus.PUBLISHED));

        PageResult<Product> page = products.findByStatus(ProductStatus.PUBLISHED, 7102L,
                PageQuery.of(0, 20, "id,asc"));

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).allSatisfy(product -> {
            assertThat(product.getSellerId()).isEqualTo(7102L);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
        });
    }

    @Test
    void aPageBeyondTheLastOneIsEmptyButKeepsTheTotal() {
        products.save(product(7104L, ProductStatus.PUBLISHED));

        PageResult<Product> page = products.findByStatus(ProductStatus.PUBLISHED, 7104L,
                PageQuery.of(50, 10, "id,asc"));

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void inventoriesAreFilteredByProductAndWarehouse() {
        inventories.save(inventory(8901L, 9001L, 5));
        inventories.save(inventory(8901L, 9002L, 7));

        PageResult<Inventory> all = inventories.findByProductId(8901L, null,
                PageQuery.of(0, 20, "quantity,desc"));
        PageResult<Inventory> one = inventories.findByProductId(8901L, 9002L,
                PageQuery.of(0, 20, "id,asc"));

        assertThat(all.content()).hasSize(2);
        assertThat(all.content().get(0).getQuantity()).isEqualTo(7);
        assertThat(one.content()).hasSize(1);
        assertThat(one.content().get(0).getWarehouseId()).isEqualTo(9002L);
    }

    @Test
    void warehousesOfASellerArePaged() {
        for (int i = 0; i < 3; i++) {
            Warehouse warehouse = new Warehouse(null, WarehouseType.SELLER);
            warehouse.setSellerId(7105L);
            warehouses.save(warehouse);
        }

        PageResult<Warehouse> page = warehouses.findBySellerId(7105L,
                PageQuery.of(0, 2, "id,asc"));

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
    }

    @Test
    void anUnknownSortFieldIsRejected() {
        assertThatThrownBy(() -> products.findByStatus(ProductStatus.PUBLISHED, 7106L,
                new PageQuery(0, 5, "password")))
                .hasMessageContaining("sort field not allowed");
    }

    private Product product(Long sellerId, ProductStatus status) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setType(ProductType.PHYSICAL);
        product.setStatus(status);
        return product;
    }

    private Inventory inventory(Long productId, Long warehouseId, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setWarehouseId(warehouseId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    @SuppressWarnings("unused")
    private List<Product> unused() {
        return List.of();
    }
}
