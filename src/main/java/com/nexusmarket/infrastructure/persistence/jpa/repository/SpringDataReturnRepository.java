package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.ReturnJpaEntity;
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
 * Technical Spring Data repository for {@link ReturnJpaEntity}.
 *
 * <p>Declares only the queries required by the
 * {@code com.nexusmarket.application.port.out.ReturnRepository} port. The
 * pessimistic lock serializes the reception of the same return.</p>
 *
 * <p>Traceability: OBJ-11 (migracion V5).</p>
 */
public interface SpringDataReturnRepository extends JpaRepository<ReturnJpaEntity, Long> {

    /** Mirrors ReturnRepository.findByOrderItemId. */
    List<ReturnJpaEntity> findByOrderItemId(Long orderItemId);

    /** Mirrors ReturnRepository.findByOrderId. */
    Page<ReturnJpaEntity> findByOrderId(Long orderId, Pageable pageable);

    /** Locks one return for the reception transaction. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReturnJpaEntity r where r.id = :id")
    Optional<ReturnJpaEntity> lockById(@Param("id") Long id);
}
