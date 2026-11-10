package com.nexusmarket.application.port.out;

import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
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

    /** Lists the stock of a product across the warehouses, optionally filtered by warehouse. */
    PageResult<Inventory> findByProductId(Long productId, Long warehouseId, PageQuery query);

    /** Lists the stock records of a product, ordered by identifier. */
    List<Inventory> findByProductId(Long productId);

    /**
     * Locks and lists the stock records of a product, ordered by identifier.
     *
     * <p>The JPA adapter takes a pessimistic write lock, so the caller must run
     * inside a transaction. Used by the payment step to deduct stock without
     * overselling.</p>
     */
    List<Inventory> lockByProductId(Long productId);

    /** Lists every stored inventory record. */
    List<Inventory> findAll();
}
