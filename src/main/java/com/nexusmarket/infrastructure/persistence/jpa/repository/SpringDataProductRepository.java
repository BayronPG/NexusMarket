package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link ProductJpaEntity}.
 *
 * <p>Declares only the queries required by the
 * {@code com.nexusmarket.application.port.out.ProductRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>Fase 1.3.1 y 2.9 (paginacion) de docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {

    /** Mirrors ProductRepository.findBySellerId. */
    Page<ProductJpaEntity> findBySellerId(Long sellerId, Pageable pageable);

    /** Mirrors ProductRepository.findByStatus. */
    Page<ProductJpaEntity> findByStatus(ProductStatus status, Pageable pageable);

    /** Filter combination used by both list endpoints. */
    Page<ProductJpaEntity> findByStatusAndSellerId(ProductStatus status, Long sellerId,
                                                   Pageable pageable);
}
