package com.nexusmarket.application.service;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.port.in.ManageInventoryUseCase;
import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.application.support.Authorization;
import com.nexusmarket.application.support.Validation;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.user.Role;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application service that administers the distributed inventory.
 *
 * <p>EXPLICIT source: the Seller and the Logistics Operator administer the
 * inventory (responsibilities matrix); inventory is linked to a product and a
 * warehouse (RN-09); stock is never negative (RN-10); nonexistent or damaged
 * stock cannot be reserved (RN-11, RN-12).</p>
 *
 * <p>The rules of the last three items are enforced by the domain entity
 * ({@link Inventory#reserve(int)} and {@link Inventory#setQuantity(int)}) and
 * are not reimplemented here.</p>
 *
 * <p>Traceability: OBJ-06, RN-09, RN-10, RN-11, RN-12, RG-03.</p>
 */
@Service
public class InventoryService implements ManageInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            WarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public Inventory registerStock(ActorContext actor, RegisterStockCommand command) {
        Authorization.requireAnyRole(actor, Role.SELLER, Role.LOGISTICS_OPERATOR);
        Validation.requireNotNull(command, "command");
        Long productId = Validation.requireNotNull(command.productId(), "productId (RN-09)");
        Long warehouseId = Validation.requireNotNull(command.warehouseId(), "warehouseId (RN-09)");

        if (productRepository.findById(productId).isEmpty()) {
            throw new NotFoundException("Product not found: " + productId);
        }
        if (warehouseRepository.findById(warehouseId).isEmpty()) {
            throw new NotFoundException("Warehouse not found: " + warehouseId);
        }
        if (inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId).isPresent()) {
            throw new BusinessRuleException(
                    "Stock is already registered for the given product and warehouse");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setWarehouseId(warehouseId);
        inventory.setQuantity(command.quantity());
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory reserveStock(ActorContext actor, ReserveStockCommand command) {
        Authorization.requireAnyRole(actor, Role.SELLER, Role.LOGISTICS_OPERATOR);
        Validation.requireNotNull(command, "command");
        Validation.requireNotNull(command.productId(), "productId");
        Validation.requireNotNull(command.warehouseId(), "warehouseId");

        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(command.productId(), command.warehouseId())
                .orElseThrow(() -> new NotFoundException(
                        "Inventory not found for the given product and warehouse"));

        inventory.reserve(command.amount());
        return inventoryRepository.save(inventory);
    }

    @Override
    public List<Inventory> findStockByProduct(ActorContext actor, Long productId) {
        Authorization.requireAnyRole(actor, Role.SELLER, Role.LOGISTICS_OPERATOR);
        return inventoryRepository.findByProductId(productId);
    }
}
