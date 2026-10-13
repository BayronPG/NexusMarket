package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.port.in.ManageRefundsUseCase.RequestRefundCommand;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryBuyerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryOrderRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryRefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the refund management use case.
 *
 * <p>Traceability: OBJ-11, responsibilities matrix (the Buyer and the
 * Administrator manage refunds), RG-03.</p>
 */
class RefundServiceTest {

    private InMemoryRefundRepository refunds;
    private InMemoryOrderRepository orders;
    private InMemoryBuyerRepository buyers;
    private RefundService service;

    private static final long BUYER_USER_ID = 100L;
    private static final long OTHER_BUYER_USER_ID = 200L;

    private final ActorContext buyer = new ActorContext(BUYER_USER_ID, Role.BUYER);
    private final ActorContext otherBuyer = new ActorContext(OTHER_BUYER_USER_ID, Role.BUYER);
    private final ActorContext seller = new ActorContext(300L, Role.SELLER);
    private final ActorContext administrator = new ActorContext(500L, Role.ADMINISTRATOR);

    private Long orderId;

    @BeforeEach
    void setUp() {
        refunds = new InMemoryRefundRepository();
        orders = new InMemoryOrderRepository();
        buyers = new InMemoryBuyerRepository();
        service = new RefundService(refunds, orders, buyers);

        Buyer storedBuyer = new Buyer();
        storedBuyer.setUserId(BUYER_USER_ID);
        Buyer buyerRecord = buyers.save(storedBuyer);

        Buyer storedOtherBuyer = new Buyer();
        storedOtherBuyer.setUserId(OTHER_BUYER_USER_ID);
        buyers.save(storedOtherBuyer);

        orderId = orders.save(new Order(null, buyerRecord.getId(), OrderStatus.DELIVERED)).getId();
    }

    @Test
    void buyerRequestsARefundForItsOwnOrder() {
        Refund refund = service.requestRefund(buyer, new RequestRefundCommand(orderId));

        assertThat(refund.getId()).isNotNull();
        assertThat(refund.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void administratorRequestsARefund() {
        Refund refund = service.requestRefund(administrator, new RequestRefundCommand(orderId));

        assertThat(refund.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void aBuyerCannotRequestARefundForAnotherBuyersOrder() {
        assertThatThrownBy(() -> service.requestRefund(otherBuyer, new RequestRefundCommand(orderId)))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void theSellerDoesNotManageRefunds() {
        assertThatThrownBy(() -> service.requestRefund(seller, new RequestRefundCommand(orderId)))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void refundRequiresAnExistingOrder() {
        assertThatThrownBy(() -> service.requestRefund(buyer, new RequestRefundCommand(999L)))
                .isInstanceOf(NotFoundException.class);
    }
}
