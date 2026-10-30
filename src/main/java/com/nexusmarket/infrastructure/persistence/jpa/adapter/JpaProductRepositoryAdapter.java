package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.ProductJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link ProductRepository} output port.
 *
 * <p>Converts the domain with {@link ProductJpaMapper} and delegates to
 * {@link SpringDataProductRepository}. Pagination is translated by
 * {@link Pageables}, so no Spring type leaks into the application layer.</p>
 *
 * <p>Fase 1.3.6 y 2.9 de docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaProductRepositoryAdapter implements ProductRepository {

    private static final Set<String> ALLOWED_SORT = Set.of("id", "status", "type", "sellerId");

    private final SpringDataProductRepository springDataProductRepository;

    public JpaProductRepositoryAdapter(SpringDataProductRepository springDataProductRepository) {
        this.springDataProductRepository = springDataProductRepository;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity saved = springDataProductRepository.save(ProductJpaMapper.toJpa(product));
        return ProductJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return springDataProductRepository.findById(id).map(ProductJpaMapper::toDomain);
    }

    @Override
    public PageResult<Product> findBySellerId(Long sellerId, ProductStatus status, PageQuery query) {
        Pageable pageable = Pageables.of(query, ALLOWED_SORT);
        Page<ProductJpaEntity> page = status == null
                ? springDataProductRepository.findBySellerId(sellerId, pageable)
                : springDataProductRepository.findByStatusAndSellerId(status, sellerId, pageable);
        return Pageables.result(page, ProductJpaMapper::toDomain);
    }

    @Override
    public PageResult<Product> findByStatus(ProductStatus status, Long sellerId, PageQuery query) {
        Pageable pageable = Pageables.of(query, ALLOWED_SORT);
        Page<ProductJpaEntity> page = sellerId == null
                ? springDataProductRepository.findByStatus(status, pageable)
                : springDataProductRepository.findByStatusAndSellerId(status, sellerId, pageable);
        return Pageables.result(page, ProductJpaMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return springDataProductRepository.findAll().stream()
                .map(ProductJpaMapper::toDomain)
                .toList();
    }
}
