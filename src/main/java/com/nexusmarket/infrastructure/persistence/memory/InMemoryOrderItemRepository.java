package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.OrderItemRepository;
import com.nexusmarket.domain.order.OrderItem;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory adapter for {@link OrderItemRepository}, used by the {@code memory}
 * profile and by the application-service tests.
 */
@Repository
@Profile("memory")
public class InMemoryOrderItemRepository implements OrderItemRepository {

    private final Map<Long, OrderItem> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public OrderItem save(OrderItem item) {
        if (item.getId() == null) {
            item.setId(sequence.incrementAndGet());
        }
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(item -> orderId != null && orderId.equals(item.getOrderId()))
                .sorted(Comparator.comparing(OrderItem::getId))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
