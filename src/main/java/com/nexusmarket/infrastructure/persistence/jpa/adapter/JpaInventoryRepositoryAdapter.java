package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.infrastructure.persistence.jpa.entity.InventoryJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.InventoryJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataInventoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link InventoryRepository} output port.
 *
 * <p>Converts the domain with {@link InventoryJpaMapper} and delegates to
 * {@link SpringDataInventoryRepository}. Pagination is translated by
 * {@link Pageables}.</p>
 *
 * <p>Fase 1.3.7 y 2.9 de docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaInventoryRepositoryAdapter implements InventoryRepository {

    private static final Set<String> ALLOWED_SORT =
            Set.of("id", "quantity", "damagedQuantity", "productId", "warehouseId");

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
    public PageResult<Inventory> findByProductId(Long productId, Long warehouseId, PageQuery query) {
        Pageable pageable = Pageables.of(query, ALLOWED_SORT);
        Page<InventoryJpaEntity> page;
        if (warehouseId == null) {
            page = springDataInventoryRepository.findByProductId(productId, pageable);
        } else {
            page = springDataInventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId,
                    pageable);
        }
        return Pageables.result(page, InventoryJpaMapper::toDomain);
    }

    @Override
    public List<Inventory> findAll() {
        return springDataInventoryRepository.findAll().stream()
                .map(InventoryJpaMapper::toDomain)
                .toList();
    }
}
