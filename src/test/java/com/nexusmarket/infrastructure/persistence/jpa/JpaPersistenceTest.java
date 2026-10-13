package com.nexusmarket.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.UserStatus;
import com.nexusmarket.domain.warehouse.WarehouseType;
import com.nexusmarket.infrastructure.persistence.jpa.entity.BuyerJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.SellerJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.entity.WarehouseJpaEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic persistence tests of the eight JPA entities, using the embedded H2
 * database through {@link TestEntityManager}. No Spring Data repository is
 * created here (that belongs to Fase 1.3).
 *
 * <p>H2 is used only for tests; the real MySQL configuration is untouched.
 * Fase 1.2 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
class JpaPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsAndReadsUser() {
        UserJpaEntity user = new UserJpaEntity();
        user.setFullName("Ada Lovelace");
        user.setEmail("ada@nexus.test");
        user.setIdentityDocument("DOC-1");
        user.setRole(Role.SELLER);
        user.setStatus(UserStatus.ACTIVE);

        Long id = entityManager.persistAndFlush(user).getId();
        entityManager.clear();

        UserJpaEntity found = entityManager.find(UserJpaEntity.class, id);
        assertThat(found.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(found.getEmail()).isEqualTo("ada@nexus.test");
        assertThat(found.getRole()).isEqualTo(Role.SELLER);
        assertThat(found.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void persistsAndReadsSeller() {
        SellerJpaEntity seller = new SellerJpaEntity();
        seller.setUserId(10L);

        Long id = entityManager.persistAndFlush(seller).getId();
        entityManager.clear();

        assertThat(entityManager.find(SellerJpaEntity.class, id).getUserId()).isEqualTo(10L);
    }

    @Test
    void persistsAndReadsBuyerWithAdditionalAddresses() {
        BuyerJpaEntity buyer = new BuyerJpaEntity();
        buyer.setUserId(11L);
        buyer.setPrimaryAddress("Calle 1");
        buyer.setAdditionalAddresses(List.of("Calle 2", "Calle 3"));
        buyer.setCommercialStatus("ACTIVE");

        Long id = entityManager.persistAndFlush(buyer).getId();
        entityManager.clear();

        BuyerJpaEntity found = entityManager.find(BuyerJpaEntity.class, id);
        assertThat(found.getPrimaryAddress()).isEqualTo("Calle 1");
        assertThat(found.getAdditionalAddresses()).containsExactlyInAnyOrder("Calle 2", "Calle 3");
    }

    @Test
    void persistsMarketplaceWarehouseWithNullSeller() {
        WarehouseJpaEntity warehouse = new WarehouseJpaEntity();
        warehouse.setType(WarehouseType.MARKETPLACE);
        warehouse.setSellerId(null);

        Long id = entityManager.persistAndFlush(warehouse).getId();
        entityManager.clear();

        WarehouseJpaEntity found = entityManager.find(WarehouseJpaEntity.class, id);
        assertThat(found.getType()).isEqualTo(WarehouseType.MARKETPLACE);
        assertThat(found.getSellerId()).isNull();
    }

    @Test
    void persistsAndReadsProductWithVariants() {
        ProductJpaEntity product = new ProductJpaEntity();
        product.setSellerId(20L);
        product.setType(ProductType.PHYSICAL);
        product.setVariants(List.of("Rojo", "Talla M"));
        product.setStatus(ProductStatus.PUBLISHED);

        Long id = entityManager.persistAndFlush(product).getId();
        entityManager.clear();

        ProductJpaEntity found = entityManager.find(ProductJpaEntity.class, id);
        assertThat(found.getType()).isEqualTo(ProductType.PHYSICAL);
        assertThat(found.getVariants()).containsExactlyInAnyOrder("Rojo", "Talla M");
        assertThat(found.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
    }

    @Test
    void persistsAndReadsInventory() {
        InventoryJpaEntity inventory = new InventoryJpaEntity();
        inventory.setProductId(6L);
        inventory.setWarehouseId(4L);
        inventory.setQuantity(100);
        inventory.setDamagedQuantity(3);

        Long id = entityManager.persistAndFlush(inventory).getId();
        entityManager.clear();

        InventoryJpaEntity found = entityManager.find(InventoryJpaEntity.class, id);
        assertThat(found.getProductId()).isEqualTo(6L);
        assertThat(found.getWarehouseId()).isEqualTo(4L);
        assertThat(found.getQuantity()).isEqualTo(100);
        assertThat(found.getDamagedQuantity()).isEqualTo(3);
    }

    @Test
    void persistsAndReadsOrder() {
        OrderJpaEntity order = new OrderJpaEntity();
        order.setBuyerId(3L);
        order.setStatus(OrderStatus.CART);

        Long id = entityManager.persistAndFlush(order).getId();
        entityManager.clear();

        OrderJpaEntity found = entityManager.find(OrderJpaEntity.class, id);
        assertThat(found.getBuyerId()).isEqualTo(3L);
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CART);
    }

    @Test
    void persistsAndReadsRefund() {
        RefundJpaEntity refund = new RefundJpaEntity();
        refund.setOrderId(8L);

        Long id = entityManager.persistAndFlush(refund).getId();
        entityManager.clear();

        assertThat(entityManager.find(RefundJpaEntity.class, id).getOrderId()).isEqualTo(8L);
    }
}
