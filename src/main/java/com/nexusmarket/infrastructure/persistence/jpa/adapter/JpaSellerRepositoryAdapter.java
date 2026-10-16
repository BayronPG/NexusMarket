package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.infrastructure.persistence.jpa.entity.SellerJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.SellerJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataSellerRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link SellerRepository} output port.
 *
 * <p>Converts the domain with {@link SellerJpaMapper} and delegates to
 * {@link SpringDataSellerRepository}. It implements only the methods declared by the
 * port and contains no business rules.</p>
 *
 * <p>Fase 1.3.3 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaSellerRepositoryAdapter implements SellerRepository {

    private final SpringDataSellerRepository springDataSellerRepository;

    public JpaSellerRepositoryAdapter(SpringDataSellerRepository springDataSellerRepository) {
        this.springDataSellerRepository = springDataSellerRepository;
    }

    @Override
    public Seller save(Seller seller) {
        SellerJpaEntity saved = springDataSellerRepository.save(SellerJpaMapper.toJpa(seller));
        return SellerJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Seller> findById(Long id) {
        return springDataSellerRepository.findById(id).map(SellerJpaMapper::toDomain);
    }

    @Override
    public Optional<Seller> findByUserId(Long userId) {
        return springDataSellerRepository.findByUserId(userId).map(SellerJpaMapper::toDomain);
    }
}
