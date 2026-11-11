package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.ReturnRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Return;
import com.nexusmarket.infrastructure.persistence.jpa.entity.ReturnJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.ReturnJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataReturnRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link ReturnRepository} output port.
 *
 * <p>Traceability: OBJ-11 (migracion V5).</p>
 */
@Repository
@Profile("!memory")
public class JpaReturnRepositoryAdapter implements ReturnRepository {

    private static final Set<String> ALLOWED_SORT =
            Set.of("id", "orderId", "orderItemId", "quantity", "status", "createdAt");

    private final SpringDataReturnRepository springDataReturnRepository;

    public JpaReturnRepositoryAdapter(SpringDataReturnRepository springDataReturnRepository) {
        this.springDataReturnRepository = springDataReturnRepository;
    }

    @Override
    public Return save(Return returnRecord) {
        ReturnJpaEntity saved = springDataReturnRepository.save(ReturnJpaMapper.toJpa(returnRecord));
        return ReturnJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Return> findById(Long id) {
        return springDataReturnRepository.findById(id).map(ReturnJpaMapper::toDomain);
    }

    @Override
    public Optional<Return> findByIdForUpdate(Long id) {
        return springDataReturnRepository.lockById(id).map(ReturnJpaMapper::toDomain);
    }

    @Override
    public List<Return> findByOrderItemId(Long orderItemId) {
        return springDataReturnRepository.findByOrderItemId(orderItemId).stream()
                .map(ReturnJpaMapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<Return> findByOrderId(Long orderId, PageQuery query) {
        Page<ReturnJpaEntity> page = springDataReturnRepository.findByOrderId(orderId,
                Pageables.of(query, ALLOWED_SORT));
        return Pageables.result(page, ReturnJpaMapper::toDomain);
    }
}
