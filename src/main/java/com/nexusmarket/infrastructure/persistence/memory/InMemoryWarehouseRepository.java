package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.domain.warehouse.Warehouse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link WarehouseRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending).</p>
 */
@Repository
public class InMemoryWarehouseRepository implements WarehouseRepository {

    private final Map<Long, Warehouse> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Warehouse save(Warehouse warehouse) {
        if (warehouse.getId() == null) {
            warehouse.setId(sequence.incrementAndGet());
        }
        store.put(warehouse.getId(), warehouse);
        return warehouse;
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Warehouse> findBySellerId(Long sellerId) {
        return store.values().stream()
                .filter(warehouse -> sellerId != null && sellerId.equals(warehouse.getSellerId()))
                .collect(Collectors.toList());
    }
}
