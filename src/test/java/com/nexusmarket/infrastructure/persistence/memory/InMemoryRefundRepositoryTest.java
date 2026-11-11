package com.nexusmarket.infrastructure.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.returns.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the temporary in-memory refund adapter.
 *
 * <p>Traceability: OBJ-11.</p>
 */
class InMemoryRefundRepositoryTest {

    private final InMemoryRefundRepository repository = new InMemoryRefundRepository();

    @Test
    void saveAssignsAnIncreasingIdentifierToNewRefunds() {
        Refund first = repository.save(refundOf(10L));
        Refund second = repository.save(refundOf(11L));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    void saveKeepsTheIdentifierOfAnExistingRefund() {
        repository.save(new Refund(50L, 1L, 10L, 5L, new BigDecimal("10.00"),
                RefundStatus.REGISTERED, LocalDateTime.now()));

        Refund updated = repository.save(new Refund(50L, 1L, 99L, 5L, new BigDecimal("10.00"),
                RefundStatus.REGISTERED, LocalDateTime.now()));

        assertThat(updated.getId()).isEqualTo(50L);
        assertThat(repository.findById(50L)).get()
                .extracting(Refund::getOrderId).isEqualTo(99L);
    }

    @Test
    void findByIdReturnsEmptyWhenTheRefundDoesNotExist() {
        assertThat(repository.findById(404L)).isEmpty();
    }

    @Test
    void findByReturnIdFindsTheSingleRefundOfAReturn() {
        repository.save(new Refund(null, 7L, 10L, 5L, new BigDecimal("10.00"),
                RefundStatus.REGISTERED, LocalDateTime.now()));

        assertThat(repository.findByReturnId(7L)).isPresent();
        assertThat(repository.findByReturnId(8L)).isEmpty();
    }

    @Test
    void findByOrderIdReturnsOnlyTheRefundsOfThatOrder() {
        repository.save(refundOf(10L));
        repository.save(refundOf(10L));
        repository.save(refundOf(11L));

        assertThat(repository.findByOrderId(10L, new PageQuery(0, 20, "id,asc")).content())
                .hasSize(2)
                .allMatch(refund -> refund.getOrderId().equals(10L));
    }

    @Test
    void findByOrderIdWithNullReturnsNoRefunds() {
        repository.save(refundOf(10L));

        assertThat(repository.findByOrderId(null, new PageQuery(0, 20, "id,asc")).content())
                .isEmpty();
    }

    private static Refund refundOf(Long orderId) {
        return new Refund(null, 1L, orderId, 5L, new BigDecimal("10.00"),
                RefundStatus.REGISTERED, LocalDateTime.now());
    }
}
