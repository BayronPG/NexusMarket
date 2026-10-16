package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.OrderJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataOrderRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link OrderRepository} output port.
 *
 * <p>Converts the domain with {@link OrderJpaMapper} and delegates to
 * {@link SpringDataOrderRepository}. It implements only the methods declared by
 * the port and contains no business rules; the {@link Order} lifecycle stays in
 * the domain.</p>
 *
 * <p>Fase 1.3.8 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaOrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;

    public JpaOrderRepositoryAdapter(SpringDataOrderRepository springDataOrderRepository) {
        this.springDataOrderRepository = springDataOrderRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity saved = springDataOrderRepository.save(OrderJpaMapper.toJpa(order));
        return OrderJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return springDataOrderRepository.findById(id).map(OrderJpaMapper::toDomain);
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        return springDataOrderRepository.findByBuyerId(buyerId).stream()
                .map(OrderJpaMapper::toDomain)
                .toList();
    }
}
