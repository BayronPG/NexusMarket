package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.domain.buyer.Buyer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link BuyerRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending).</p>
 */
@Repository
@Profile("memory")
public class InMemoryBuyerRepository implements BuyerRepository {

    private final Map<Long, Buyer> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Buyer save(Buyer buyer) {
        if (buyer.getId() == null) {
            buyer.setId(sequence.incrementAndGet());
        }
        store.put(buyer.getId(), buyer);
        return buyer;
    }

    @Override
    public Optional<Buyer> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Buyer> findByUserId(Long userId) {
        return store.values().stream()
                .filter(buyer -> userId != null && userId.equals(buyer.getUserId()))
                .findFirst();
    }
}
