package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link RefundJpaEntity}.
 *
 * <p>Declares only the query required by the
 * {@code com.nexusmarket.application.port.out.RefundRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>Fase 1.3.1 of docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataRefundRepository extends JpaRepository<RefundJpaEntity, Long> {

    /** Mirrors RefundRepository.findByOrderId. */
    List<RefundJpaEntity> findByOrderId(Long orderId);
}
