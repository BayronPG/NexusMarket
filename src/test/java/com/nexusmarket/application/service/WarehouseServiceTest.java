package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.ManageWarehousesUseCase.RegisterCommand;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import com.nexusmarket.infrastructure.persistence.memory.InMemorySellerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryWarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the warehouse administration use case.
 *
 * <p>Traceability: OBJ-04, general flow step 1 (the Administrator registers
 * the seller and its first warehouse), RG-03.</p>
 */
class WarehouseServiceTest {

    private InMemoryWarehouseRepository warehouses;
    private InMemorySellerRepository sellers;
    private WarehouseService service;

    private final ActorContext administrator = new ActorContext(1L, Role.ADMINISTRATOR);
    private final ActorContext sellerActor = new ActorContext(2L, Role.SELLER);

    @BeforeEach
    void setUp() {
        warehouses = new InMemoryWarehouseRepository();
        sellers = new InMemorySellerRepository();
        service = new WarehouseService(warehouses, sellers);
    }

    @Test
    void administratorRegistersMarketplaceWarehouse() {
        Warehouse warehouse = service.registerWarehouse(administrator,
                new RegisterCommand(WarehouseType.MARKETPLACE, null));

        assertThat(warehouse.getId()).isNotNull();
        assertThat(warehouse.getType()).isEqualTo(WarehouseType.MARKETPLACE);
        assertThat(warehouse.getSellerId()).isNull();
    }

    @Test
    void administratorRegistersSellerWarehouseOfAnExistingSeller() {
        Seller seller = new Seller();
        seller.setUserId(2L);
        Seller stored = sellers.save(seller);

        Warehouse warehouse = service.registerWarehouse(administrator,
                new RegisterCommand(WarehouseType.SELLER, stored.getId()));

        assertThat(warehouse.getSellerId()).isEqualTo(stored.getId());
    }

    @Test
    void sellerWarehouseRequiresAnExistingSeller() {
        assertThatThrownBy(() -> service.registerWarehouse(administrator,
                new RegisterCommand(WarehouseType.SELLER, 999L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void sellerWarehouseRequiresASellerId() {
        assertThatThrownBy(() -> service.registerWarehouse(administrator,
                new RegisterCommand(WarehouseType.SELLER, null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void marketplaceWarehouseCannotBelongToASeller() {
        assertThatThrownBy(() -> service.registerWarehouse(administrator,
                new RegisterCommand(WarehouseType.MARKETPLACE, 1L)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void onlyTheAdministratorRegistersWarehouses() {
        assertThatThrownBy(() -> service.registerWarehouse(sellerActor,
                new RegisterCommand(WarehouseType.MARKETPLACE, null)))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void listsTheWarehousesOfASeller() {
        Seller seller = new Seller();
        seller.setUserId(2L);
        Seller stored = sellers.save(seller);
        service.registerWarehouse(administrator, new RegisterCommand(WarehouseType.SELLER, stored.getId()));

        assertThat(service.findWarehousesBySeller(administrator, stored.getId())).hasSize(1);
    }
}
