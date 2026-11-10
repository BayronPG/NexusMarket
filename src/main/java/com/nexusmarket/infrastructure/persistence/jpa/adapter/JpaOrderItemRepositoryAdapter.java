package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.OrderItemRepository;
import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.OrderItemJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataOrderItemRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link OrderItemRepository} output port.
 *
 * <p>Traceability: OBJ-07, OBJ-08.</p>
 */
@Repository
@Profile("!memory")
public class JpaOrderItemRepositoryAdapter implements OrderItemRepository {

    private final SpringDataOrderItemRepository springDataOrderItemRepository;

    public JpaOrderItemRepositoryAdapter(
            SpringDataOrderItemRepository springDataOrderItemRepository) {
        this.springDataOrderItemRepository = springDataOrderItemRepository;
    }

    @Override
    public OrderItem save(OrderItem item) {
        return OrderItemJpaMapper.toDomain(
                springDataOrderItemRepository.save(OrderItemJpaMapper.toJpa(item)));
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return springDataOrderItemRepository.findById(id).map(OrderItemJpaMapper::toDomain);
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return springDataOrderItemRepository.findByOrderIdOrderByIdAsc(orderId).stream()
                .map(OrderItemJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataOrderItemRepository.deleteById(id);
    }
}
