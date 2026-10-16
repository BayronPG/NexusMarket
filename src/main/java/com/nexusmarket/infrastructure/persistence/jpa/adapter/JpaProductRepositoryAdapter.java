package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.ProductJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link ProductRepository} output port.
 *
 * <p>Converts the domain with {@link ProductJpaMapper} and delegates to
 * {@link SpringDataProductRepository}. It implements only the methods declared by
 * the port and contains no business rules.</p>
 *
 * <p>Fase 1.3.6 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaProductRepositoryAdapter implements ProductRepository {

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
    public List<Product> findBySellerId(Long sellerId) {
        return springDataProductRepository.findBySellerId(sellerId).stream()
                .map(ProductJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return springDataProductRepository.findByStatus(status).stream()
                .map(ProductJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAll() {
        return springDataProductRepository.findAll().stream()
                .map(ProductJpaMapper::toDomain)
                .toList();
    }
}
