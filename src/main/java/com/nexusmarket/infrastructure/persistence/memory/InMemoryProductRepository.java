package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
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
 * persistence strategy (D-004 pending).</p>
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
    public List<Product> findBySellerId(Long sellerId) {
        return store.values().stream()
                .filter(product -> sellerId != null && sellerId.equals(product.getSellerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return store.values().stream()
                .filter(product -> product.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }
}
