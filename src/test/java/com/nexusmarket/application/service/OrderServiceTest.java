package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryBuyerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the order lifecycle use case.
 *
 * <p>Traceability: OBJ-07, OBJ-08, RN-13 (a finalized order cannot be
 * modified), RN-16 (only the defined transitions), RG-03.</p>
 */
class OrderServiceTest {

    private InMemoryOrderRepository orders;
    private InMemoryBuyerRepository buyers;
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
        service = new OrderService(orders, buyers);

        buyers.save(buyerOf(BUYER_USER_ID));
        buyers.save(buyerOf(OTHER_BUYER_USER_ID));
    }

    @Test
    void buyerCreatesAnOrderInCartStatus() {
        Order order = service.createOrder(buyer);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CART);
        assertThat(order.getBuyerId()).isNotNull();
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
    void followsTheDefinedLifecycle() {
        Order order = service.createOrder(buyer);

        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PENDING_PAYMENT);
        service.changeOrderStatus(buyer, order.getId(), OrderStatus.PAID);
        service.changeOrderStatus(logisticsOperator, order.getId(), OrderStatus.SHIPPED);
        Order delivered = service.changeOrderStatus(logisticsOperator, order.getId(), OrderStatus.DELIVERED);

        assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void anUndefinedTransitionIsRejected() {
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.changeOrderStatus(buyer, order.getId(), OrderStatus.SHIPPED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RN-16");
    }

    @Test
    void aFinalizedOrderCannotBeModified() {
        Order order = service.createOrder(buyer);
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

        assertThatThrownBy(() -> service.changeOrderStatus(otherBuyer, order.getId(), OrderStatus.PENDING_PAYMENT))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void theAdministratorDoesNotManageOrders() {
        Order order = service.createOrder(buyer);

        assertThatThrownBy(() -> service.changeOrderStatus(administrator, order.getId(), OrderStatus.PENDING_PAYMENT))
                .isInstanceOf(AuthorizationException.class);
    }

    private static Buyer buyerOf(long userId) {
        Buyer buyer = new Buyer();
        buyer.setUserId(userId);
        return buyer;
    }
}
