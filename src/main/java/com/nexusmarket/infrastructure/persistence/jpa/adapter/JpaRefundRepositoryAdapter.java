package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.RefundJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataRefundRepository;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link RefundRepository} output port.
 *
 * <p>Traceability: OBJ-11.</p>
 */
@Repository
@Profile("!memory")
public class JpaRefundRepositoryAdapter implements RefundRepository {

    private static final Set<String> ALLOWED_SORT =
            Set.of("id", "returnId", "orderId", "orderItemId", "amount", "createdAt");

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
    public Optional<Refund> findByReturnId(Long returnId) {
        return springDataRefundRepository.findByReturnId(returnId)
                .map(RefundJpaMapper::toDomain);
    }

    @Override
    public PageResult<Refund> findByOrderId(Long orderId, PageQuery query) {
        Page<RefundJpaEntity> page = springDataRefundRepository.findByOrderId(orderId,
                Pageables.of(query, ALLOWED_SORT));
        return Pageables.result(page, RefundJpaMapper::toDomain);
    }
}
