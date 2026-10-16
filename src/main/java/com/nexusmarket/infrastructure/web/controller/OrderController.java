package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageOrdersUseCase;
import com.nexusmarket.infrastructure.web.dto.ChangeOrderStatusRequest;
import com.nexusmarket.infrastructure.web.dto.OrderResponse;
import com.nexusmarket.infrastructure.web.mapper.OrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link ManageOrdersUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final ManageOrdersUseCase manageOrdersUseCase;

    public OrderController(ManageOrdersUseCase manageOrdersUseCase) {
        this.manageOrdersUseCase = manageOrdersUseCase;
    }

    @PostMapping
    public OrderResponse create(ActorContext actor) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.createOrder(actor));
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse changeStatus(
            ActorContext actor,
            @PathVariable Long orderId,
            @Valid @RequestBody ChangeOrderStatusRequest request) {
        return OrderWebMapper.toResponse(
                manageOrdersUseCase.changeOrderStatus(actor, orderId, request.status()));
    }

    @GetMapping("/{orderId}")
    public OrderResponse find(ActorContext actor, @PathVariable Long orderId) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.findOrder(actor, orderId));
    }
}
