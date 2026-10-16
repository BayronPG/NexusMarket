package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link InventoryJpaEntity}.
 *
 * <p>Declares only the queries required by the
 * {@code com.nexusmarket.application.port.out.InventoryRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>Fase 1.3.1 of docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataInventoryRepository extends JpaRepository<InventoryJpaEntity, Long> {

    /** Mirrors InventoryRepository.findByProductIdAndWarehouseId. */
    Optional<InventoryJpaEntity> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /** Mirrors InventoryRepository.findByProductId (distributed inventory). */
    List<InventoryJpaEntity> findByProductId(Long productId);
}
