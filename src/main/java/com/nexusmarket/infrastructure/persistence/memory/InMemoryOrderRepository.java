package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.domain.order.Order;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link OrderRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending).</p>
 */
@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(sequence.incrementAndGet());
        }
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        return store.values().stream()
                .filter(order -> buyerId != null && buyerId.equals(order.getBuyerId()))
                .collect(Collectors.toList());
    }
}
