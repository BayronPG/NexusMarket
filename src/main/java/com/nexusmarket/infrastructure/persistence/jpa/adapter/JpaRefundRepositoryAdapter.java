package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.RefundJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataRefundRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link RefundRepository} output port.
 *
 * <p>Converts the domain with {@link RefundJpaMapper} and delegates to
 * {@link SpringDataRefundRepository}. It implements only the methods declared by
 * the port and contains no business rules.</p>
 *
 * <p>Fase 1.3.9 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaRefundRepositoryAdapter implements RefundRepository {

    private final SpringDataRefundRepository springDataRefundRepository;

    public JpaRefundRepositoryAdapter(SpringDataRefundRepository springDataRefundRepository) {
        this.springDataRefundRepository = springDataRefundRepository;
    }

    @Override
    public Refund save(Refund refund) {
        RefundJpaEntity saved = springDataRefundRepository.save(RefundJpaMapper.toJpa(refund));
        return RefundJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Refund> findById(Long id) {
        return springDataRefundRepository.findById(id).map(RefundJpaMapper::toDomain);
    }

    @Override
    public List<Refund> findByOrderId(Long orderId) {
        return springDataRefundRepository.findByOrderId(orderId).stream()
                .map(RefundJpaMapper::toDomain)
                .toList();
    }
}
