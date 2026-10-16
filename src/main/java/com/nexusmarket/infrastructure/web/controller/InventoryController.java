package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageInventoryUseCase;
import com.nexusmarket.infrastructure.web.dto.InventoryResponse;
import com.nexusmarket.infrastructure.web.dto.RegisterStockRequest;
import com.nexusmarket.infrastructure.web.dto.ReserveStockRequest;
import com.nexusmarket.infrastructure.web.mapper.InventoryWebMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link ManageInventoryUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final ManageInventoryUseCase manageInventoryUseCase;

    public InventoryController(ManageInventoryUseCase manageInventoryUseCase) {
        this.manageInventoryUseCase = manageInventoryUseCase;
    }

    @PostMapping
    public InventoryResponse register(ActorContext actor, @Valid @RequestBody RegisterStockRequest request) {
        return InventoryWebMapper.toResponse(manageInventoryUseCase.registerStock(
                actor, InventoryWebMapper.toRegisterStockCommand(request)));
    }

    @PostMapping("/reservations")
    public InventoryResponse reserve(ActorContext actor, @Valid @RequestBody ReserveStockRequest request) {
        return InventoryWebMapper.toResponse(manageInventoryUseCase.reserveStock(
                actor, InventoryWebMapper.toReserveStockCommand(request)));
    }

    @GetMapping
    public List<InventoryResponse> findByProduct(ActorContext actor, @RequestParam Long productId) {
        return InventoryWebMapper.toResponses(
                manageInventoryUseCase.findStockByProduct(actor, productId));
    }
}
