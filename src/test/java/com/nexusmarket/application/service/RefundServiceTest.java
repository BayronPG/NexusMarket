package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.returns.RefundStatus;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryBuyerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryOrderRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryRefundRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the refund query use case.
 *
 * <p>Traceability: OBJ-11, RG-03.</p>
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
    private Long refundId;

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
        refundId = refunds.save(refundOf(orderId)).getId();
    }

    @Test
    void buyerReadsItsOwnRefund() {
        Refund refund = service.findRefund(buyer, refundId);

        assertThat(refund.getOrderId()).isEqualTo(orderId);
        assertThat(refund.getAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void administratorReadsAnyRefund() {
        assertThat(service.findRefund(administrator, refundId).getId()).isEqualTo(refundId);
    }

    @Test
    void aBuyerCannotReadAnotherBuyersRefund() {
        assertThatThrownBy(() -> service.findRefund(otherBuyer, refundId))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void theSellerDoesNotReadRefunds() {
        assertThatThrownBy(() -> service.findRefund(seller, refundId))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void anUnknownRefundIsReportedAsNotFound() {
        assertThatThrownBy(() -> service.findRefund(buyer, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listsTheRefundsOfAnOrder() {
        assertThat(service.findRefundsByOrder(buyer, orderId, new PageQuery(0, 20, "id,asc"))
                .content()).hasSize(1);
    }

    @Test
    void aBuyerCannotListRefundsOfAnotherBuyersOrder() {
        assertThatThrownBy(() -> service.findRefundsByOrder(
                otherBuyer, orderId, new PageQuery(0, 20, "id,asc")))
                .isInstanceOf(AuthorizationException.class);
    }

    private static Refund refundOf(Long orderId) {
        return new Refund(null, 7L, orderId, 9L, new BigDecimal("20.00"),
                RefundStatus.REGISTERED, LocalDateTime.now());
    }
}
