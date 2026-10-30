package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
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
 * TEMPORARY in-memory adapter for {@link ProductRepository}.
 *
 * <p>See {@link InMemoryUserRepository} for the reason of this temporary
 * persistence strategy (D-004 pending). Pagination mirrors the JPA adapter so
 * the {@code memory} profile behaves like the real one.</p>
 */
@Repository
@Profile("memory")
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(sequence.incrementAndGet());
        }
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public PageResult<Product> findBySellerId(Long sellerId, ProductStatus status, PageQuery query) {
        return page(status, sellerId, query);
    }

    @Override
    public PageResult<Product> findByStatus(ProductStatus status, Long sellerId, PageQuery query) {
        return page(status, sellerId, query);
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    private PageResult<Product> page(ProductStatus status, Long sellerId, PageQuery query) {
        List<Product> filtered = store.values().stream()
                .filter(product -> status == null || product.getStatus() == status)
                .filter(product -> sellerId == null || sellerId.equals(product.getSellerId()))
                .sorted(comparator(query))
                .collect(Collectors.toList());
        if (query.descending()) {
            Collections.reverse(filtered);
        }
        return PageResult.slice(filtered, query);
    }

    private Comparator<Product> comparator(PageQuery query) {
        return switch (query.field()) {
            case "id" -> Comparator.comparing(Product::getId, Comparator.nullsLast(Long::compareTo));
            case "status" -> Comparator.comparing(product -> product.getStatus().name());
            case "type" -> Comparator.comparing(product -> product.getType().name());
            case "sellerId" ->
                    Comparator.comparing(Product::getSellerId, Comparator.nullsLast(Long::compareTo));
            default -> throw new IllegalArgumentException("sort field not allowed: " + query.field());
        };
    }
}
