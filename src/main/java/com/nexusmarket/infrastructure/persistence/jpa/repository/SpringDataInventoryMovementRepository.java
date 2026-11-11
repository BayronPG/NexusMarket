package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link InventoryMovementJpaEntity}.
 *
 * <p>Traceability: OBJ-06, OBJ-11 (migracion V5).</p>
 */
public interface SpringDataInventoryMovementRepository
        extends JpaRepository<InventoryMovementJpaEntity, Long> {
}
