package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.BuyerJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link BuyerJpaEntity}.
 *
 * <p>Declares only the query required by the
 * {@code com.nexusmarket.application.port.out.BuyerRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>Fase 1.3.1 of docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataBuyerRepository extends JpaRepository<BuyerJpaEntity, Long> {

    /** Mirrors BuyerRepository.findByUserId. */
    Optional<BuyerJpaEntity> findByUserId(Long userId);
}
