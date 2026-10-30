package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.infrastructure.persistence.jpa.entity.WarehouseJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.WarehouseJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataWarehouseRepository;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link WarehouseRepository} output port.
 *
 * <p>Converts the domain with {@link WarehouseJpaMapper} and delegates to
 * {@link SpringDataWarehouseRepository}. Pagination is translated by
 * {@link Pageables}.</p>
 *
 * <p>Fase 1.3.5 y 2.9 de docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaWarehouseRepositoryAdapter implements WarehouseRepository {

    private static final Set<String> ALLOWED_SORT = Set.of("id", "type", "sellerId");

    private final SpringDataWarehouseRepository springDataWarehouseRepository;

    public JpaWarehouseRepositoryAdapter(SpringDataWarehouseRepository springDataWarehouseRepository) {
        this.springDataWarehouseRepository = springDataWarehouseRepository;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseJpaEntity saved = springDataWarehouseRepository.save(WarehouseJpaMapper.toJpa(warehouse));
        return WarehouseJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Warehouse> findById(Long id) {
        return springDataWarehouseRepository.findById(id).map(WarehouseJpaMapper::toDomain);
    }

    @Override
    public PageResult<Warehouse> findBySellerId(Long sellerId, PageQuery query) {
        Pageable pageable = Pageables.of(query, ALLOWED_SORT);
        Page<WarehouseJpaEntity> page = springDataWarehouseRepository.findBySellerId(sellerId, pageable);
        return Pageables.result(page, WarehouseJpaMapper::toDomain);
    }
}
