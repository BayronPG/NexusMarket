package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link ProductJpaEntity}.
 *
 * <p>Declares only the queries required by the
 * {@code com.nexusmarket.application.port.out.ProductRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>NOTE: {@code findByStatus} uses the shared enum {@link ProductStatus}, which is
 * the declared type of the entity attribute {@code ProductJpaEntity.status}; the
 * technical layer depends on the domain for this enum (infrastructure -&gt; domain).</p>
 *
 * <p>Fase 1.3.1 of docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {

    /** Mirrors ProductRepository.findBySellerId. */
    List<ProductJpaEntity> findBySellerId(Long sellerId);

    /** Mirrors ProductRepository.findByStatus. */
    List<ProductJpaEntity> findByStatus(ProductStatus status);
}
