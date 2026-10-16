package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataSellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaSellerRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.3 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaSellerRepositoryAdapter.class)
class JpaSellerRepositoryAdapterTest {

    @Autowired
    private JpaSellerRepositoryAdapter adapter;

    @Autowired
    private SpringDataSellerRepository springDataSellerRepository;

    @BeforeEach
    void setUp() {
        springDataSellerRepository.deleteAll();
    }

    private Seller newSeller() {
        Seller seller = new Seller();
        seller.setUserId(100L);
        return seller;
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        Seller saved = adapter.save(newSeller());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(100L);
        assertThat(springDataSellerRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        Seller saved = adapter.save(newSeller());

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Seller::getUserId)
                .isEqualTo(100L);
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findByUserIdReturnsTheDomainWhenPresent() {
        Seller saved = adapter.save(newSeller());

        assertThat(adapter.findByUserId(100L))
                .isPresent()
                .get()
                .extracting(Seller::getId)
                .isEqualTo(saved.getId());
    }

    @Test
    void findByUserIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findByUserId(404L)).isEmpty();
    }
}
