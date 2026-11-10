package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryBuyerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryInventoryRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryOrderItemRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryOrderRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the order lifecycle and the cart lines.
 *
 * <p>Traceability: OBJ-07, OBJ-08, RN-13, RN-16, RG-03.</p>
 */
class OrderServiceTest {

    private InMemoryOrderRepository orders;
    private InMemoryBuyerRepository buyers;
    private InMemoryProductRepository products;
    private InMemoryInventoryRepository inventories;
    private InMemoryOrderItemRepository items;
    private OrderService service;

    private static final long BUYER_USER_ID = 100L;
    private static final long OTHER_BUYER_USER_ID = 200L;

    private final ActorContext buyer = new ActorContext(BUYER_USER_ID, Role.BUYER);
    private final ActorContext otherBuyer = new ActorContext(OTHER_BUYER_USER_ID, Role.BUYER);
    private final ActorContext seller = new ActorContext(300L, Role.SELLER);
    private final ActorContext logisticsOperator = new ActorContext(400L, Role.LOGISTICS_OPERATOR);
    private final ActorContext administrator = new ActorContext(500L, Role.ADMINISTRATOR);

    @BeforeEach
    void setUp() {
        orders = new InMemoryOrderRepository();
        buyers = new InMemoryBuyerRepository();
        products = new InMemoryProductRepository();
        inventories = new InMemoryInventoryRepository();
        items = new InMemoryOrderItemRepository();
        service = new OrderService(orders, buyers, products, inventories, items);

        buyers.save(buyerOf(BUYER_USER_ID));
        buyers.save(buyerOf(OTHER_BUYER_USER_ID));
    }

    @Test
    void buyerCreatesAnOrderInCartStatus() {
        Order order = service.createOrder(buyer);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CART);
        assertThat(order.getBuyerId()).isNotNull();
        assertThat(order.getItems()).isEmpty();
        assertThat(order.total()).isEqualByComparingTo("0.00");
    }

    @Test
    void onlyBuyersCreateOrders() {
        assertThatThrownBy(() -> service.createOrder(seller))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void buyerWithoutBuyerRecordCannotCreateOrders() {
        ActorContext unknownBuyer = new ActorContext(999L, Role.BUYER);

        assertThatThrownBy(() -> service.createOrder(unknownBuyer))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addingAPublishedProductStoresThePriceSnapshot() {
        long productId = publishedProduct("10.50");
        stock(productId, 10);
        Order order = service.createOrder(buyer);

        Order updated = service.addItem(buyer, order.getId(), productId, 2);

        assertThat(updated.getItems()).hasSize(1);
        assertThat(updated.getItems().get(0).getUnitPrice()).isEqualByComparingTo("10.50");
        assertThat(updated.total()).isEqualByComparingTo("21.00");
    }

    @Test
    void addingTheSameProductIncreasesTheQuantityWithoutDuplicatingTheLine() {
        long productId = publishedProduct("10.50");
        stock(productId, 10);
        Order order = service.createOrder(buyer);

        service.addItem(buyer, order.getId(), productId, 1);
        Order updated = service.addItem(buyer, order.getId(), productId, 2);

        assertThat(updated.getItems()).hasSize(1);
        assertThat(updated.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(updated.getItems().get(0).subtotal()).isEqualByComparingTo("31.50");
    }

    @Test
    void anUnpublishedProductCannotBeAdded() {
        Product product = new Product(null, ProductType.PHYSICAL, ProductStatus.SUSPENDED,
                new BigDecimal("10.00"));
        long productId = products.save(product).getId();
        stock(productId, 5);
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.addItem(buyer, order.getId(), productId, 1))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void theQuantityMustBePositive() {
        long productId = publishedProduct("10.00");
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.addItem(buyer, order.getId(), productId, 0))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.addItem(buyer, order.getId(), productId, -1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void theQuantityCannotExceedTheAvailableStock() {
        long productId = publishedProduct("10.00");
        stock(productId, 3);
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.addItem(buyer, order.getId(), productId, 4))
                .isInstanceOf(BusinessRuleException.class);

        service.addItem(buyer, order.getId(), productId, 2);
        assertThatThrownBy(() -> service.addItem(buyer, order.getId(), productId, 2))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updatingTheQuantityRecalculatesTheTotal() {
        long productId = publishedProduct("10.00");
        stock(productId, 10);
        Order order = service.createOrder(buyer);
        Order withItem = service.addItem(buyer, order.getId(), productId, 1);
        long itemId = withItem.getItems().get(0).getId();

        Order updated = service.updateItem(buyer, order.getId(), itemId, 4);

        assertThat(updated.getItems().get(0).getQuantity()).isEqualTo(4);
        assertThat(updated.total()).isEqualByComparingTo("40.00");
    }

    @Test
    void updatingTheQuantityAboveTheStockIsRejected() {
        long productId = publishedProduct("10.00");
        stock(productId, 5);
        Order order = service.createOrder(buyer);
        long itemId = service.addItem(buyer, order.getId(), productId, 1).getItems().get(0).getId();

        assertThatThrownBy(() -> service.updateItem(buyer, order.getId(), itemId, 6))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void anItemCanBeRemoved() {
        long productId = publishedProduct("10.00");
        stock(productId, 5);
        Order order = service.createOrder(buyer);
        long itemId = service.addItem(buyer, order.getId(), productId, 2).getItems().get(0).getId();

        Order updated = service.removeItem(buyer, order.getId(), itemId);

        assertThat(updated.getItems()).isEmpty();
        assertThat(updated.total()).isEqualByComparingTo("0.00");
    }

    @Test
    void theTotalIsTheSumOfTheLineSubtotals() {
        long first = publishedProduct("10.00");
        long second = publishedProduct("2.50");
        stock(first, 10);
        stock(second, 10);
        Order order = service.createOrder(buyer);

        service.addItem(buyer, order.getId(), first, 1);
        Order updated = service.addItem(buyer, order.getId(), second, 3);

        assertThat(updated.getItems()).hasSize(2);
        assertThat(updated.total()).isEqualByComparingTo("17.50");
        assertThat(updated.getItems().get(1).subtotal()).isEqualByComparingTo("7.50");
    }

    @Test
    void thePriceSnapshotIsNotAffectedByLaterCatalogChanges() {
        long productId = publishedProduct("10.00");
        stock(productId, 10);
        Order order = service.createOrder(buyer);
        service.addItem(buyer, order.getId(), productId, 1);

        Product product = products.findById(productId).orElseThrow();
        product.setPrice(new BigDecimal("99.99"));
        products.save(product);

        Order reloaded = service.findOrder(buyer, order.getId());

        assertThat(reloaded.getItems().get(0).getUnitPrice()).isEqualByComparingTo("10.00");
        assertThat(reloaded.total()).isEqualByComparingTo("10.00");
    }

    @Test
    void itemsCannotBeModifiedOutsideCart() {
        long productId = publishedProduct("10.00");
        stock(productId, 10);
        Order order = service.createOrder(buyer);
        service.addItem(buyer, order.getId(), productId, 1);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PENDING_PAYMENT);

        assertThatThrownBy(() -> service.addItem(buyer, order.getId(), productId, 1))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void followsTheDefinedLifecycle() {
        long productId = publishedProduct("10.00");
        stock(productId, 10);
        Order order = service.createOrder(buyer);
        service.addItem(buyer, order.getId(), productId, 1);

        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PENDING_PAYMENT);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID);
        service.changeOrderStatus(logisticsOperator, order.getId(), OrderStatus.SHIPPED);
        Order delivered = service.changeOrderStatus(logisticsOperator, order.getId(), OrderStatus.DELIVERED);

        assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void anEmptyCartCannotBePaid() {
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.changeOrderStatus(
                buyer, order.getId(), OrderStatus.PENDING_PAYMENT))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void payingDeductsInventoryOnlyOnce() {
        long productId = publishedProduct("10.00");
        stock(productId, 5);
        Order order = service.createOrder(buyer);
        service.addItem(buyer, order.getId(), productId, 2);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PENDING_PAYMENT);

        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID);
        assertThat(availableStock(productId)).isEqualTo(3);

        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID);
        assertThat(availableStock(productId)).isEqualTo(3);
    }

    @Test
    void paymentRejectsWhenTheStockWasConsumedMeanwhile() {
        long productId = publishedProduct("10.00");
        stock(productId, 2);
        Order order = service.createOrder(buyer);
        service.addItem(buyer, order.getId(), productId, 2);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PENDING_PAYMENT);

        Inventory inventory = inventories.findByProductId(productId).get(0);
        inventory.setQuantity(1);
        inventories.save(inventory);

        assertThatThrownBy(() -> service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void payingAcrossWarehousesDeductsFromTheFirstRecordOnwards() {
        long productId = publishedProduct("10.00");
        stock(productId, 1);
        Inventory second = new Inventory(null, productId, 99L, 5);
        inventories.save(second);
        Order order = service.createOrder(buyer);
        service.addItem(buyer, order.getId(), productId, 4);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PENDING_PAYMENT);

        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID);

        assertThat(availableStock(productId)).isEqualTo(2);
    }

    @Test
    void anUndefinedTransitionIsRejected() {
        Order order = service.createOrder(buyer);
        long productId = publishedProduct("10.00");
        stock(productId, 5);
        service.addItem(buyer, order.getId(), productId, 1);

        assertThatThrownBy(() -> service.changeOrderStatus(buyer, order.getId(), OrderStatus.SHIPPED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RN-16");
    }

    @Test
    void aFinalizedOrderCannotBeModified() {
        long productId = publishedProduct("10.00");
        stock(productId, 5);
        Order order = service.createOrder(buyer);
        service.addItem(buyer, order.getId(), productId, 1);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PENDING_PAYMENT);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID);
        service.changeOrderStatus(seller, order.getId(), OrderStatus.SHIPPED);
        service.changeOrderStatus(seller, order.getId(), OrderStatus.DELIVERED);

        assertThatThrownBy(() -> service.changeOrderStatus(seller, order.getId(), OrderStatus.PAID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RN-13");
    }

    @Test
    void aBuyerCannotChangeAnotherBuyersOrder() {
        Order order = service.createOrder(buyer);
        long productId = publishedProduct("10.00");
        stock(productId, 5);

        assertThatThrownBy(() -> service.addItem(otherBuyer, order.getId(), productId, 1))
                .isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> service.changeOrderStatus(
                otherBuyer, order.getId(), OrderStatus.PENDING_PAYMENT))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void rolesOtherThanBuyerCannotEditTheCart() {
        Order order = service.createOrder(buyer);
        long productId = publishedProduct("10.00");
        stock(productId, 5);

        assertThatThrownBy(() -> service.addItem(seller, order.getId(), productId, 1))
                .isInstanceOf(AuthorizationException.class);
        assertThatThrownBy(() -> service.addItem(administrator, order.getId(), productId, 1))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void theAdministratorDoesNotManageOrders() {
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.changeOrderStatus(
                administrator, order.getId(), OrderStatus.PENDING_PAYMENT))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void anUnknownItemIsReportedAsNotFound() {
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.updateItem(buyer, order.getId(), 999L, 1))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.removeItem(buyer, order.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    private long publishedProduct(String price) {
        return products.save(new Product(null, ProductType.PHYSICAL, ProductStatus.PUBLISHED,
                new BigDecimal(price))).getId();
    }

    private void stock(long productId, int quantity) {
        inventories.save(new Inventory(null, productId, 1L, quantity));
    }

    private int availableStock(long productId) {
        return inventories.findByProductId(productId).stream()
                .mapToInt(inventory -> inventory.getQuantity() - inventory.getDamagedQuantity())
                .sum();
    }

    private static Buyer buyerOf(long userId) {
        Buyer buyer = new Buyer();
        buyer.setUserId(userId);
        return buyer;
    }
}
