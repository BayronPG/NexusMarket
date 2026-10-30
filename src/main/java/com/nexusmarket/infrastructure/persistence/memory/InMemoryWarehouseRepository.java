package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.warehouse.Warehouse;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link WarehouseRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending). Pagination mirrors the JPA adapter.</p>
 */
@Repository
@Profile("memory")
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
    public PageResult<Warehouse> findBySellerId(Long sellerId, PageQuery query) {
        List<Warehouse> filtered = store.values().stream()
                .filter(warehouse -> sellerId == null || sellerId.equals(warehouse.getSellerId()))
                .sorted(comparator(query))
                .collect(Collectors.toList());
        if (query.descending()) {
            Collections.reverse(filtered);
        }
        return PageResult.slice(filtered, query);
    }

    private Comparator<Warehouse> comparator(PageQuery query) {
        return switch (query.field()) {
            case "id" -> Comparator.comparing(Warehouse::getId, Comparator.nullsLast(Long::compareTo));
            case "type" -> Comparator.comparing(warehouse -> warehouse.getType().name());
            case "sellerId" ->
                    Comparator.comparing(Warehouse::getSellerId, Comparator.nullsLast(Long::compareTo));
            default -> throw new IllegalArgumentException("sort field not allowed: " + query.field());
        };
    }
}
