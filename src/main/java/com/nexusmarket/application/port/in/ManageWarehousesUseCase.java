package com.nexusmarket.application.port.in;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import java.util.List;

/**
 * Inbound port: administration of warehouses.
 *
 * <p>EXPLICIT source: the general flow registers, as its first step, the
 * seller and its first warehouse by the Administrator; the specification
 * classifies warehouses as Marketplace or Seller ones.</p>
 *
 * <p>Traceability: OBJ-04, RG-03.</p>
 */
public interface ManageWarehousesUseCase {

    /**
     * Input data of the registration.
     *
     * @param type     required classification of the warehouse
     * @param sellerId owner seller; required for SELLER warehouses and absent for MARKETPLACE ones
     */
    record RegisterCommand(WarehouseType type, Long sellerId) {
    }

    /**
     * Registers a warehouse.
     *
     * @param actor   authenticated actor; must hold the ADMINISTRATOR role
     * @param command input data
     * @return the created warehouse
     */
    Warehouse registerWarehouse(ActorContext actor, RegisterCommand command);

    /**
     * Lists the warehouses of a seller.
     *
     * @param actor    authenticated actor holding the ADMINISTRATOR role
     * @param sellerId seller identifier
     * @param query    pagination and sort
     * @return one page of warehouses
     */
    PageResult<Warehouse> findWarehousesBySeller(ActorContext actor, Long sellerId, PageQuery query);
}
