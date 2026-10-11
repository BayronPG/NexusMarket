package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.domain.inventory.Inventory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link InventoryRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending).</p>
 */
@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<Long, Inventory> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Inventory save(Inventory inventory) {
        if (inventory.getId() == null) {
            inventory.setId(sequence.incrementAndGet());
        }
        store.put(inventory.getId(), inventory);
        return inventory;
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        return store.values().stream()
                .filter(inventory -> productId != null && productId.equals(inventory.getProductId())
                        && warehouseId != null && warehouseId.equals(inventory.getWarehouseId()))
                .findFirst();
    }

    @Override
    public List<Inventory> findByProductId(Long productId) {
        return store.values().stream()
                .filter(inventory -> productId != null && productId.equals(inventory.getProductId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findAll() {
        return List.copyOf(store.values());
    }
}
