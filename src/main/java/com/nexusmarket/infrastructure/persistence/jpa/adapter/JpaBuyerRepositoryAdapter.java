package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.infrastructure.persistence.jpa.entity.BuyerJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.BuyerJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataBuyerRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link BuyerRepository} output port.
 *
 * <p>Converts the domain with {@link BuyerJpaMapper} and delegates to
 * {@link SpringDataBuyerRepository}. It implements only the methods declared by
 * the port and contains no business rules.</p>
 *
 * <p>Fase 1.3.4 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaBuyerRepositoryAdapter implements BuyerRepository {

    private final SpringDataBuyerRepository springDataBuyerRepository;

    public JpaBuyerRepositoryAdapter(SpringDataBuyerRepository springDataBuyerRepository) {
        this.springDataBuyerRepository = springDataBuyerRepository;
    }

    @Override
    public Buyer save(Buyer buyer) {
        BuyerJpaEntity saved = springDataBuyerRepository.save(BuyerJpaMapper.toJpa(buyer));
        return BuyerJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Buyer> findById(Long id) {
        return springDataBuyerRepository.findById(id).map(BuyerJpaMapper::toDomain);
    }

    @Override
    public Optional<Buyer> findByUserId(Long userId) {
        return springDataBuyerRepository.findByUserId(userId).map(BuyerJpaMapper::toDomain);
    }
}
