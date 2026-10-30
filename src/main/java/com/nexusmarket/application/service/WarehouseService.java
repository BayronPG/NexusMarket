package com.nexusmarket.application.service;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.ManageWarehousesUseCase;
import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.application.support.Authorization;
import com.nexusmarket.application.support.Validation;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application service that administers warehouses.
 *
 * <p>EXPLICIT source: the Administrator registers the seller and its first
 * warehouse (general flow, step 1); warehouses are classified as Marketplace
 * or Seller ones.</p>
 *
 * <p>INTERPRETATION (consistent with the domain model): a SELLER warehouse
 * must reference an existing seller, while a MARKETPLACE warehouse has no
 * owner.</p>
 *
 * <p>Traceability: OBJ-04, RG-03.</p>
 */
@Service
public class WarehouseService implements ManageWarehousesUseCase {

    private final WarehouseRepository warehouseRepository;
    private final SellerRepository sellerRepository;

    public WarehouseService(WarehouseRepository warehouseRepository, SellerRepository sellerRepository) {
        this.warehouseRepository = warehouseRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public Warehouse registerWarehouse(ActorContext actor, RegisterCommand command) {
        Authorization.requireAnyRole(actor, Role.ADMINISTRATOR);
        Validation.requireNotNull(command, "command");
        WarehouseType type = Validation.requireNotNull(command.type(), "type");

        if (type == WarehouseType.SELLER) {
            Long sellerId = Validation.requireNotNull(command.sellerId(), "sellerId (required for a SELLER warehouse)");
            if (sellerRepository.findById(sellerId).isEmpty()) {
                throw new NotFoundException("Seller not found: " + sellerId);
            }
        } else if (command.sellerId() != null) {
            throw new ValidationException("A MARKETPLACE warehouse does not belong to a seller");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setType(type);
        warehouse.setSellerId(command.sellerId());
        return warehouseRepository.save(warehouse);
    }

    @Override
    public PageResult<Warehouse> findWarehousesBySeller(ActorContext actor, Long sellerId, PageQuery query) {
        Authorization.requireAnyRole(actor, Role.ADMINISTRATOR);
        return warehouseRepository.findBySellerId(sellerId, query);
    }
}
