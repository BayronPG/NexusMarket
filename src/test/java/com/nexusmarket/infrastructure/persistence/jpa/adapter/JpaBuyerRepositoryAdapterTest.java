package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataBuyerRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaBuyerRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.4 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaBuyerRepositoryAdapter.class)
class JpaBuyerRepositoryAdapterTest {

    @Autowired
    private JpaBuyerRepositoryAdapter adapter;

    @Autowired
    private SpringDataBuyerRepository springDataBuyerRepository;

    @BeforeEach
    void setUp() {
        springDataBuyerRepository.deleteAll();
    }

    private Buyer newBuyer() {
        Buyer buyer = new Buyer();
        buyer.setUserId(200L);
        buyer.setPrimaryAddress("Calle 10 #20-30");
        buyer.setAdditionalAddresses(List.of("Carrera 40 #5-10"));
        buyer.setCommercialStatus("ACTIVE");
        return buyer;
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        Buyer saved = adapter.save(newBuyer());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(200L);
        assertThat(springDataBuyerRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void savePersistsTheAdditionalAddressesList() {
        Buyer saved = adapter.save(newBuyer());

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Buyer::getAdditionalAddresses)
                .isEqualTo(List.of("Carrera 40 #5-10"));
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        Buyer saved = adapter.save(newBuyer());

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Buyer::getPrimaryAddress, Buyer::getCommercialStatus)
                .containsExactly("Calle 10 #20-30", "ACTIVE");
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findByUserIdReturnsTheDomainWhenPresent() {
        Buyer saved = adapter.save(newBuyer());

        assertThat(adapter.findByUserId(200L))
                .isPresent()
                .get()
                .extracting(Buyer::getId)
                .isEqualTo(saved.getId());
    }

    @Test
    void findByUserIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findByUserId(404L)).isEmpty();
    }
}
