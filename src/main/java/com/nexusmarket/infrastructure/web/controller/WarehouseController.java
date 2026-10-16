package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageWarehousesUseCase;
import com.nexusmarket.infrastructure.web.dto.CreateWarehouseRequest;
import com.nexusmarket.infrastructure.web.dto.WarehouseResponse;
import com.nexusmarket.infrastructure.web.mapper.WarehouseWebMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link ManageWarehousesUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final ManageWarehousesUseCase manageWarehousesUseCase;

    public WarehouseController(ManageWarehousesUseCase manageWarehousesUseCase) {
        this.manageWarehousesUseCase = manageWarehousesUseCase;
    }

    @PostMapping
    public WarehouseResponse register(ActorContext actor, @Valid @RequestBody CreateWarehouseRequest request) {
        return WarehouseWebMapper.toResponse(
                manageWarehousesUseCase.registerWarehouse(actor, WarehouseWebMapper.toCommand(request)));
    }

    @GetMapping
    public List<WarehouseResponse> findBySeller(ActorContext actor, @RequestParam Long sellerId) {
        return WarehouseWebMapper.toResponses(
                manageWarehousesUseCase.findWarehousesBySeller(actor, sellerId));
    }
}
