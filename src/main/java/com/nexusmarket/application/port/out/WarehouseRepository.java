package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.warehouse.Warehouse;
import java.util.List;
import java.util.Optional;

/**
 * Output port for warehouse persistence.
 *
 * <p>Traceability: OBJ-04.</p>
 */
public interface WarehouseRepository {

    /** Stores the warehouse, assigning an identifier when it is new. */
    Warehouse save(Warehouse warehouse);

    /** Finds a warehouse by its identifier. */
    Optional<Warehouse> findById(Long id);

    /** Lists the warehouses owned by a seller. */
    List<Warehouse> findBySellerId(Long sellerId);
}
