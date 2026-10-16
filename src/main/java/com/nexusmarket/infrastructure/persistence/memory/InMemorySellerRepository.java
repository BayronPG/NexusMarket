package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.domain.seller.Seller;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link SellerRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending).</p>
 */
@Repository
@Profile("memory")
public class InMemorySellerRepository implements SellerRepository {

    private final Map<Long, Seller> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Seller save(Seller seller) {
        if (seller.getId() == null) {
            seller.setId(sequence.incrementAndGet());
        }
        store.put(seller.getId(), seller);
        return seller;
    }

    @Override
    public Optional<Seller> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Seller> findByUserId(Long userId) {
        return store.values().stream()
                .filter(seller -> userId != null && userId.equals(seller.getUserId()))
                .findFirst();
    }
}
