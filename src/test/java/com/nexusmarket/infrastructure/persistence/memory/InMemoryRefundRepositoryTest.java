package com.nexusmarket.infrastructure.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.returns.Refund;
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
        Refund first = repository.save(new Refund(null, 10L));
        Refund second = repository.save(new Refund(null, 11L));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    void saveKeepsTheIdentifierOfAnExistingRefund() {
        repository.save(new Refund(50L, 10L));

        Refund updated = repository.save(new Refund(50L, 99L));

        assertThat(updated.getId()).isEqualTo(50L);
        assertThat(repository.findById(50L)).get()
                .extracting(Refund::getOrderId).isEqualTo(99L);
    }

    @Test
    void findByIdReturnsEmptyWhenTheRefundDoesNotExist() {
        assertThat(repository.findById(404L)).isEmpty();
    }

    @Test
    void findByOrderIdReturnsOnlyTheRefundsOfThatOrder() {
        repository.save(new Refund(null, 10L));
        repository.save(new Refund(null, 10L));
        repository.save(new Refund(null, 11L));

        assertThat(repository.findByOrderId(10L)).hasSize(2)
                .allMatch(refund -> refund.getOrderId().equals(10L));
    }

    @Test
    void findByOrderIdWithNullReturnsNoRefunds() {
        repository.save(new Refund(null, 10L));

        assertThat(repository.findByOrderId(null)).isEmpty();
    }
}
