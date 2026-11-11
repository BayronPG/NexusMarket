package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.ReturnRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Return;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * In-memory adapter for {@link ReturnRepository}, used by the {@code memory}
 * profile and by the application-service tests.
 */
@Repository
@Profile("memory")
public class InMemoryReturnRepository implements ReturnRepository {

    private final Map<Long, Return> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Return save(Return returnRecord) {
        if (returnRecord.getId() == null) {
            returnRecord.setId(sequence.incrementAndGet());
        }
        store.put(returnRecord.getId(), returnRecord);
        return returnRecord;
    }

    @Override
    public Optional<Return> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Return> findByIdForUpdate(Long id) {
        return findById(id);
    }

    @Override
    public List<Return> findByOrderItemId(Long orderItemId) {
        return store.values().stream()
                .filter(current -> orderItemId != null && orderItemId.equals(current.getOrderItemId()))
                .sorted(Comparator.comparing(Return::getId))
                .toList();
    }

    @Override
    public PageResult<Return> findByOrderId(Long orderId, PageQuery query) {
        List<Return> matching = store.values().stream()
                .filter(current -> orderId != null && orderId.equals(current.getOrderId()))
                .sorted(comparator(query))
                .toList();
        return PageResult.slice(matching, query);
    }

    private Comparator<Return> comparator(PageQuery query) {
        Comparator<Return> comparator = switch (query.field()) {
            case "id" -> Comparator.comparing(Return::getId);
            case "orderId" -> Comparator.comparing(Return::getOrderId);
            case "orderItemId" -> Comparator.comparing(Return::getOrderItemId);
            case "quantity" -> Comparator.comparingInt(Return::getQuantity);
            case "status" -> Comparator.comparing(Return::getStatus);
            case "createdAt" -> Comparator.comparing(Return::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> throw new IllegalArgumentException(
                    "sort field not allowed: " + query.field());
        };
        return query.descending() ? comparator.reversed() : comparator;
    }
}
