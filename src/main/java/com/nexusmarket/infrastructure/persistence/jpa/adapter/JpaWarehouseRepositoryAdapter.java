package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.infrastructure.persistence.jpa.entity.WarehouseJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.WarehouseJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataWarehouseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link WarehouseRepository} output port.
 *
 * <p>Converts the domain with {@link WarehouseJpaMapper} and delegates to
 * {@link SpringDataWarehouseRepository}. It implements only the methods declared
 * by the port and contains no business rules.</p>
 *
 * <p>Fase 1.3.5 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaWarehouseRepositoryAdapter implements WarehouseRepository {

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
    public List<Warehouse> findBySellerId(Long sellerId) {
        return springDataWarehouseRepository.findBySellerId(sellerId).stream()
                .map(WarehouseJpaMapper::toDomain)
                .toList();
    }
}
