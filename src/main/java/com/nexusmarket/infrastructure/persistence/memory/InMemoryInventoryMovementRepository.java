package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.InventoryMovementRepository;
import com.nexusmarket.domain.inventory.InventoryMovement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory adapter for {@link InventoryMovementRepository}, used by the
 * {@code memory} profile and by the application-service tests.
 */
@Repository
@Profile("memory")
public class InMemoryInventoryMovementRepository implements InventoryMovementRepository {

    private final Map<Long, InventoryMovement> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        if (movement.getId() == null) {
            movement.setId(sequence.incrementAndGet());
        }
        store.put(movement.getId(), movement);
        return movement;
    }
}
