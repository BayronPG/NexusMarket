package com.nexusmarket.application.port.in;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.inventory.Inventory;
import java.util.List;

/**
 * Inbound port: administration of the distributed inventory.
 *
 * <p>EXPLICIT source: inventory administration is a responsibility of the
 * Seller and the Logistics Operator (responsibilities matrix). Inventory is
 * linked to a product and a warehouse (RN-09); stock is never negative
 * (RN-10); nonexistent or damaged stock cannot be reserved (RN-11, RN-12).</p>
 *
 * <p>Traceability: OBJ-06, RN-09, RN-10, RN-11, RN-12, RG-03.</p>
 */
public interface ManageInventoryUseCase {

    /**
     * Input data to register initial stock.
     *
     * @param productId   required product (RN-09)
     * @param warehouseId required warehouse (RN-09)
     * @param quantity    non negative stock (RN-10)
     */
    record RegisterStockCommand(Long productId, Long warehouseId, int quantity) {
    }

    /**
     * Input data to reserve stock.
     *
     * @param productId   product of the inventory record
     * @param warehouseId warehouse of the inventory record
     * @param amount      quantity to reserve; must exist and not be damaged (RN-11, RN-12)
     */
    record ReserveStockCommand(Long productId, Long warehouseId, int amount) {
    }

    /**
     * Registers initial stock for a product in a warehouse.
     *
     * @param actor   authenticated actor; SELLER or LOGISTICS_OPERATOR
     * @param command input data
     * @return the created inventory record
     */
    Inventory registerStock(ActorContext actor, RegisterStockCommand command);

    /**
     * Reserves stock of a product in a warehouse.
     *
     * @param actor   authenticated actor; SELLER or LOGISTICS_OPERATOR
     * @param command input data
     * @return the updated inventory record
     */
    Inventory reserveStock(ActorContext actor, ReserveStockCommand command);

    /**
     * Lists the stock of a product across the warehouses (distributed inventory).
     *
     * @param actor       authenticated actor; SELLER or LOGISTICS_OPERATOR
     * @param productId   product identifier
     * @param warehouseId optional warehouse filter; null means every warehouse
     * @param query       pagination and sort
     * @return one page of inventory records
     */
    PageResult<Inventory> findStockByProduct(ActorContext actor, Long productId, Long warehouseId,
                                             PageQuery query);
}
