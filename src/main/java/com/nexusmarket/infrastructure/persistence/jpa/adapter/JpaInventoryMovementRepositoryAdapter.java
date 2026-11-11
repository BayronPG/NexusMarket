package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.InventoryMovementRepository;
import com.nexusmarket.domain.inventory.InventoryMovement;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.InventoryMovementJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataInventoryMovementRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link InventoryMovementRepository} output port.
 *
 * <p>Traceability: OBJ-06, OBJ-11.</p>
 */
@Repository
@Profile("!memory")
public class JpaInventoryMovementRepositoryAdapter implements InventoryMovementRepository {

    private final SpringDataInventoryMovementRepository springDataInventoryMovementRepository;

    public JpaInventoryMovementRepositoryAdapter(
            SpringDataInventoryMovementRepository springDataInventoryMovementRepository) {
        this.springDataInventoryMovementRepository = springDataInventoryMovementRepository;
    }

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        return InventoryMovementJpaMapper.toDomain(
                springDataInventoryMovementRepository.save(
                        InventoryMovementJpaMapper.toJpa(movement)));
    }
}
