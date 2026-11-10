package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.OrderItemJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link OrderItemJpaEntity}.
 *
 * <p>Traceability: OBJ-07, OBJ-08.</p>
 */
public interface SpringDataOrderItemRepository extends JpaRepository<OrderItemJpaEntity, Long> {

    /** Mirrors OrderItemRepository.findByOrderId. */
    List<OrderItemJpaEntity> findByOrderIdOrderByIdAsc(Long orderId);
}
