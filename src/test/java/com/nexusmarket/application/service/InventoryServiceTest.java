package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.port.in.ManageInventoryUseCase.RegisterStockCommand;
import com.nexusmarket.application.port.in.ManageInventoryUseCase.ReserveStockCommand;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryInventoryRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryProductRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryWarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the inventory administration use case.
 *
 * <p>Traceability: OBJ-06, RN-09 (linked to a product and a warehouse),
 * RN-10 (no negative stock), RN-11 (no reserving nonexistent stock),
 * RN-12 (no reserving damaged stock), responsibilities matrix.</p>
 */
class InventoryServiceTest {

    private InMemoryInventoryRepository inventories;
    private InMemoryProductRepository products;
    private InMemoryWarehouseRepository warehouses;
    private InventoryService service;

    private Long productId;
    private Long warehouseId;

    private final ActorContext seller = new ActorContext(1L, Role.SELLER);
    private final ActorContext logisticsOperator = new ActorContext(2L, Role.LOGISTICS_OPERATOR);
    private final ActorContext buyer = new ActorContext(3L, Role.BUYER);

    @BeforeEach
    void setUp() {
        inventories = new InMemoryInventoryRepository();
        products = new InMemoryProductRepository();
        warehouses = new InMemoryWarehouseRepository();
        service = new InventoryService(inventories, products, warehouses);

        productId = products.save(new Product(null, ProductType.PHYSICAL, ProductStatus.PUBLISHED)).getId();
        warehouseId = warehouses.save(new Warehouse(null, WarehouseType.MARKETPLACE)).getId();
    }

    @Test
    void registersInitialStockForProductAndWarehouse() {
        Inventory inventory = service.registerStock(seller, new RegisterStockCommand(productId, warehouseId, 10));

        assertThat(inventory.getId()).isNotNull();
        assertThat(inventory.getQuantity()).isEqualTo(10);
        assertThat(inventory.getProductId()).isEqualTo(productId);
        assertThat(inventory.getWarehouseId()).isEqualTo(warehouseId);
    }

    @Test
    void theLogisticsOperatorCanAdministerInventory() {
        Inventory inventory = service.registerStock(logisticsOperator,
                new RegisterStockCommand(productId, warehouseId, 5));

        assertThat(inventory.getQuantity()).isEqualTo(5);
    }

    @Test
    void theBuyerCannotAdministerInventory() {
        assertThatThrownBy(() -> service.registerStock(buyer,
                new RegisterStockCommand(productId, warehouseId, 5)))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void negativeStockIsRejectedByTheDomain() {
        assertThatThrownBy(() -> service.registerStock(seller,
                new RegisterStockCommand(productId, warehouseId, -1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RN-10");
    }

    @Test
    void stockRequiresAnExistingProduct() {
        assertThatThrownBy(() -> service.registerStock(seller,
                new RegisterStockCommand(999L, warehouseId, 5)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void stockRequiresAnExistingWarehouse() {
        assertThatThrownBy(() -> service.registerStock(seller,
                new RegisterStockCommand(productId, 999L, 5)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void stockCannotBeRegisteredTwiceForTheSameProductAndWarehouse() {
        service.registerStock(seller, new RegisterStockCommand(productId, warehouseId, 5));

        assertThatThrownBy(() -> service.registerStock(seller,
                new RegisterStockCommand(productId, warehouseId, 5)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void reservationReducesTheAvailableStock() {
        service.registerStock(seller, new RegisterStockCommand(productId, warehouseId, 10));

        Inventory reserved = service.reserveStock(seller, new ReserveStockCommand(productId, warehouseId, 4));

        assertThat(reserved.getQuantity()).isEqualTo(6);
    }

    @Test
    void nonexistentStockCannotBeReserved() {
        service.registerStock(seller, new RegisterStockCommand(productId, warehouseId, 3));

        assertThatThrownBy(() -> service.reserveStock(seller, new ReserveStockCommand(productId, warehouseId, 4)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RN-11");
    }

    @Test
    void damagedStockCannotBeReserved() {
        Inventory inventory = new Inventory(null, productId, warehouseId, 5);
        inventory.setDamagedQuantity(5);
        inventories.save(inventory);

        assertThatThrownBy(() -> service.reserveStock(seller, new ReserveStockCommand(productId, warehouseId, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RN-12");
    }

    @Test
    void reservationRequiresAnExistingInventoryRecord() {
        assertThatThrownBy(() -> service.reserveStock(seller, new ReserveStockCommand(productId, warehouseId, 1)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listsTheDistributedStockOfAProduct() {
        service.registerStock(seller, new RegisterStockCommand(productId, warehouseId, 5));
        Long anotherWarehouse = warehouses.save(new Warehouse(null, WarehouseType.MARKETPLACE)).getId();
        service.registerStock(seller, new RegisterStockCommand(productId, anotherWarehouse, 7));

        assertThat(service.findStockByProduct(seller, productId)).hasSize(2);
    }
}
