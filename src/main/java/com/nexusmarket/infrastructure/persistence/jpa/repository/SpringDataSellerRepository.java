package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.SellerJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link SellerJpaEntity}.
 *
 * <p>Declares only the query required by the
 * {@code com.nexusmarket.application.port.out.SellerRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>Fase 1.3.1 of docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataSellerRepository extends JpaRepository<SellerJpaEntity, Long> {

    /** Mirrors SellerRepository.findByUserId. */
    Optional<SellerJpaEntity> findByUserId(Long userId);
}
