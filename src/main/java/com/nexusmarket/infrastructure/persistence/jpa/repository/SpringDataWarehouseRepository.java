package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.WarehouseJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link WarehouseJpaEntity}.
 *
 * <p>Declares only the query required by the
 * {@code com.nexusmarket.application.port.out.WarehouseRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>Fase 1.3.1 y 2.9 (paginacion) de docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataWarehouseRepository extends JpaRepository<WarehouseJpaEntity, Long> {

    /** Mirrors WarehouseRepository.findBySellerId. */
    Page<WarehouseJpaEntity> findBySellerId(Long sellerId, Pageable pageable);
}
