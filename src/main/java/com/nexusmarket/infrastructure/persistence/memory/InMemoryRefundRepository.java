package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.domain.returns.Refund;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link RefundRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending).</p>
 */
@Repository
@Profile("memory")
public class InMemoryRefundRepository implements RefundRepository {

    private final Map<Long, Refund> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Refund save(Refund refund) {
        if (refund.getId() == null) {
            refund.setId(sequence.incrementAndGet());
        }
        store.put(refund.getId(), refund);
        return refund;
    }

    @Override
    public Optional<Refund> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Refund> findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(refund -> orderId != null && orderId.equals(refund.getOrderId()))
                .collect(Collectors.toList());
    }
}
