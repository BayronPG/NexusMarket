package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataRefundRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaRefundRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.9 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaRefundRepositoryAdapter.class)
class JpaRefundRepositoryAdapterTest {

    @Autowired
    private JpaRefundRepositoryAdapter adapter;

    @Autowired
    private SpringDataRefundRepository springDataRefundRepository;

    @BeforeEach
    void setUp() {
        springDataRefundRepository.deleteAll();
    }

    private Refund newRefund(Long orderId) {
        Refund refund = new Refund();
        refund.setOrderId(orderId);
        return refund;
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        Refund saved = adapter.save(newRefund(500L));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderId()).isEqualTo(500L);
        assertThat(springDataRefundRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        Refund saved = adapter.save(newRefund(500L));

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Refund::getOrderId)
                .isEqualTo(500L);
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findByOrderIdReturnsTheRefundsOfTheOrder() {
        adapter.save(newRefund(500L));
        adapter.save(newRefund(500L));
        adapter.save(newRefund(600L));

        List<Refund> result = adapter.findByOrderId(500L);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(
                refund -> assertThat(refund.getOrderId()).isEqualTo(500L));
    }

    @Test
    void findByOrderIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findByOrderId(404L)).isEmpty();
    }
}
