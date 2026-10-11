package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.inventory.Inventory;
import java.util.List;
import java.util.Optional;

/**
 * Output port for distributed inventory persistence.
 *
 * <p>Traceability: OBJ-06, RN-09.</p>
 */
public interface InventoryRepository {

    /** Stores the inventory record, assigning an identifier when it is new. */
    Inventory save(Inventory inventory);

    /** Finds an inventory record by its identifier. */
    Optional<Inventory> findById(Long id);

    /** Finds the stock of a product in a concrete warehouse (RN-09). */
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    /** Lists the stock of a product across every warehouse (distributed inventory). */
    List<Inventory> findByProductId(Long productId);

    /** Lists every stored inventory record. */
    List<Inventory> findAll();
}
