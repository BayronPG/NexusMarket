package com.nexusmarket.infrastructure.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.BuyerJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.InventoryJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.OrderJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.ProductJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.RefundJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.SellerJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.UserJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.WarehouseJpaMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests of the explicit domain &lt;-&gt; JPA mappers.
 *
 * <p>Every mapper is verified in both directions so that persistence neither
 * loses nor invents data. Fase 1.2 of docs/PLAN_API_WEB.md.</p>
 */
class JpaMapperTest {

    @Test
    void userMappingRoundTripPreservesAttributes() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Ada Lovelace");
        user.setEmail("ada@nexus.test");
        user.setIdentityDocument("DOC-1");
        user.setRole(Role.SELLER);
        user.setStatus(UserStatus.ACTIVE);

        User roundTripped = UserJpaMapper.toDomain(UserJpaMapper.toJpa(user));

        assertThat(roundTripped.getId()).isEqualTo(1L);
        assertThat(roundTripped.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(roundTripped.getEmail()).isEqualTo("ada@nexus.test");
        assertThat(roundTripped.getIdentityDocument()).isEqualTo("DOC-1");
        assertThat(roundTripped.getRole()).isEqualTo(Role.SELLER);
        assertThat(roundTripped.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void sellerMappingRoundTripPreservesUserId() {
        Seller seller = new Seller();
        seller.setId(2L);
        seller.setUserId(10L);

        Seller roundTripped = SellerJpaMapper.toDomain(SellerJpaMapper.toJpa(seller));

        assertThat(roundTripped.getId()).isEqualTo(2L);
        assertThat(roundTripped.getUserId()).isEqualTo(10L);
    }

    @Test
    void buyerMappingRoundTripPreservesAddresses() {
        Buyer buyer = new Buyer();
        buyer.setId(3L);
        buyer.setUserId(11L);
        buyer.setPrimaryAddress("Calle 1");
        buyer.setAdditionalAddresses(List.of("Calle 2", "Calle 3"));
        buyer.setCommercialStatus("ACTIVE");

        Buyer roundTripped = BuyerJpaMapper.toDomain(BuyerJpaMapper.toJpa(buyer));

        assertThat(roundTripped.getId()).isEqualTo(3L);
        assertThat(roundTripped.getUserId()).isEqualTo(11L);
        assertThat(roundTripped.getPrimaryAddress()).isEqualTo("Calle 1");
        assertThat(roundTripped.getAdditionalAddresses()).containsExactly("Calle 2", "Calle 3");
        assertThat(roundTripped.getCommercialStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void warehouseMappingRoundTripPreservesNullSellerForMarketplace() {
        Warehouse marketplace = new Warehouse();
        marketplace.setId(4L);
        marketplace.setType(WarehouseType.MARKETPLACE);
        marketplace.setSellerId(null);

        Warehouse roundTripped = WarehouseJpaMapper.toDomain(WarehouseJpaMapper.toJpa(marketplace));

        assertThat(roundTripped.getId()).isEqualTo(4L);
        assertThat(roundTripped.getType()).isEqualTo(WarehouseType.MARKETPLACE);
        assertThat(roundTripped.getSellerId()).isNull();
    }

    @Test
    void warehouseMappingRoundTripPreservesSellerForSellerWarehouse() {
        Warehouse sellerWarehouse = new Warehouse();
        sellerWarehouse.setId(5L);
        sellerWarehouse.setType(WarehouseType.SELLER);
        sellerWarehouse.setSellerId(20L);

        Warehouse roundTripped = WarehouseJpaMapper.toDomain(WarehouseJpaMapper.toJpa(sellerWarehouse));

        assertThat(roundTripped.getType()).isEqualTo(WarehouseType.SELLER);
        assertThat(roundTripped.getSellerId()).isEqualTo(20L);
    }

    @Test
    void productMappingRoundTripPreservesVariantsAndStatus() {
        Product product = new Product();
        product.setId(6L);
        product.setSellerId(20L);
        product.setType(ProductType.PHYSICAL);
        product.setVariants(List.of("Rojo", "Talla M"));
        product.setStatus(ProductStatus.PUBLISHED);

        Product roundTripped = ProductJpaMapper.toDomain(ProductJpaMapper.toJpa(product));

        assertThat(roundTripped.getId()).isEqualTo(6L);
        assertThat(roundTripped.getSellerId()).isEqualTo(20L);
        assertThat(roundTripped.getType()).isEqualTo(ProductType.PHYSICAL);
        assertThat(roundTripped.getVariants()).containsExactly("Rojo", "Talla M");
        assertThat(roundTripped.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
    }

    @Test
    void inventoryMappingRoundTripPreservesQuantities() {
        Inventory inventory = new Inventory();
        inventory.setId(7L);
        inventory.setProductId(6L);
        inventory.setWarehouseId(4L);
        inventory.setQuantity(100);
        inventory.setDamagedQuantity(3);

        Inventory roundTripped = InventoryJpaMapper.toDomain(InventoryJpaMapper.toJpa(inventory));

        assertThat(roundTripped.getId()).isEqualTo(7L);
        assertThat(roundTripped.getProductId()).isEqualTo(6L);
        assertThat(roundTripped.getWarehouseId()).isEqualTo(4L);
        assertThat(roundTripped.getQuantity()).isEqualTo(100);
        assertThat(roundTripped.getDamagedQuantity()).isEqualTo(3);
    }

    @Test
    void orderMappingRoundTripPreservesStatus() {
        Order order = new Order(8L, 3L, OrderStatus.CART);

        Order roundTripped = OrderJpaMapper.toDomain(OrderJpaMapper.toJpa(order));

        assertThat(roundTripped.getId()).isEqualTo(8L);
        assertThat(roundTripped.getBuyerId()).isEqualTo(3L);
        assertThat(roundTripped.getStatus()).isEqualTo(OrderStatus.CART);
    }

    @Test
    void refundMappingRoundTripPreservesOrderId() {
        Refund refund = new Refund();
        refund.setId(9L);
        refund.setOrderId(8L);

        Refund roundTripped = RefundJpaMapper.toDomain(RefundJpaMapper.toJpa(refund));

        assertThat(roundTripped.getId()).isEqualTo(9L);
        assertThat(roundTripped.getOrderId()).isEqualTo(8L);
    }

    @Test
    void mappersAreNullSafe() {
        assertThat(UserJpaMapper.toJpa(null)).isNull();
        assertThat(UserJpaMapper.toDomain(null)).isNull();
        assertThat(OrderJpaMapper.toJpa(null)).isNull();
        assertThat(OrderJpaMapper.toDomain(null)).isNull();
    }
}
