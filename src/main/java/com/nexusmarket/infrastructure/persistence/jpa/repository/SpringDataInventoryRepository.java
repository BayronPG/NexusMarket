package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Technical Spring Data repository for {@link InventoryJpaEntity}.
 *
 * <p>Declares only the queries required by the
 * {@code com.nexusmarket.application.port.out.InventoryRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>Fase 1.3.1 y 2.9 (paginacion) de docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataInventoryRepository extends JpaRepository<InventoryJpaEntity, Long> {

    /** Mirrors InventoryRepository.findByProductIdAndWarehouseId. */
    Optional<InventoryJpaEntity> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /** Mirrors InventoryRepository.findByProductId. */
    Page<InventoryJpaEntity> findByProductId(Long productId, Pageable pageable);

    /** Optional warehouse filter of the inventory listing. */
    Page<InventoryJpaEntity> findByWarehouseId(Long warehouseId, Pageable pageable);

    /** Filter combination of the inventory listing. */
    Page<InventoryJpaEntity> findByProductIdAndWarehouseId(Long productId, Long warehouseId,
                                                           Pageable pageable);

    /** Mirrors InventoryRepository.findByProductId(Long). */
    List<InventoryJpaEntity> findByProductIdOrderByIdAsc(Long productId);

    /**
     * Mirrors InventoryRepository.lockByProductId. The pessimistic write lock
     * prevents two concurrent payments from deducting the same stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryJpaEntity i where i.productId = :productId order by i.id asc")
    List<InventoryJpaEntity> lockByProductId(@Param("productId") Long productId);
}
