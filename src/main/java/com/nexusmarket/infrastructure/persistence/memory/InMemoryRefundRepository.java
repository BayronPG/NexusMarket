package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Refund;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
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
    public Optional<Refund> findByReturnId(Long returnId) {
        return store.values().stream()
                .filter(refund -> returnId != null && returnId.equals(refund.getReturnId()))
                .findFirst();
    }

    @Override
    public PageResult<Refund> findByOrderId(Long orderId, PageQuery query) {
        List<Refund> matching = store.values().stream()
                .filter(refund -> orderId != null && orderId.equals(refund.getOrderId()))
                .sorted(comparator(query))
                .toList();
        return PageResult.slice(matching, query);
    }

    private Comparator<Refund> comparator(PageQuery query) {
        Comparator<Refund> comparator = switch (query.field()) {
            case "id" -> Comparator.comparing(Refund::getId);
            case "returnId" -> Comparator.comparing(Refund::getReturnId);
            case "orderId" -> Comparator.comparing(Refund::getOrderId);
            case "orderItemId" -> Comparator.comparing(Refund::getOrderItemId);
            case "amount" -> Comparator.comparing(Refund::getAmount);
            case "createdAt" -> Comparator.comparing(Refund::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> throw new IllegalArgumentException(
                    "sort field not allowed: " + query.field());
        };
        return query.descending() ? comparator.reversed() : comparator;
    }
}
