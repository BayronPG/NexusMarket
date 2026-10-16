package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.InventoryJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataInventoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link InventoryRepository} output port.
 *
 * <p>Converts the domain with {@link InventoryJpaMapper} and delegates to
 * {@link SpringDataInventoryRepository}. It implements only the methods declared
 * by the port and contains no business rules.</p>
 *
 * <p>Fase 1.3.7 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaInventoryRepositoryAdapter implements InventoryRepository {

    private final SpringDataInventoryRepository springDataInventoryRepository;

    public JpaInventoryRepositoryAdapter(SpringDataInventoryRepository springDataInventoryRepository) {
        this.springDataInventoryRepository = springDataInventoryRepository;
    }

    @Override
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity saved = springDataInventoryRepository.save(InventoryJpaMapper.toJpa(inventory));
        return InventoryJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return springDataInventoryRepository.findById(id).map(InventoryJpaMapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        return springDataInventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .map(InventoryJpaMapper::toDomain);
    }

    @Override
    public List<Inventory> findByProductId(Long productId) {
        return springDataInventoryRepository.findByProductId(productId).stream()
                .map(InventoryJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Inventory> findAll() {
        return springDataInventoryRepository.findAll().stream()
                .map(InventoryJpaMapper::toDomain)
                .toList();
    }
}
