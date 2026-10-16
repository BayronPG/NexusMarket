package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.RegisterSellerUseCase;
import com.nexusmarket.infrastructure.web.dto.CreateSellerRequest;
import com.nexusmarket.infrastructure.web.dto.SellerResponse;
import com.nexusmarket.infrastructure.web.mapper.SellerWebMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link RegisterSellerUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md. It depends only on the input port, never
 * on a concrete service or a JPA entity, and it exposes DTOs instead of domain
 * objects (section 3.2).</p>
 */
@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final RegisterSellerUseCase registerSellerUseCase;

    public SellerController(RegisterSellerUseCase registerSellerUseCase) {
        this.registerSellerUseCase = registerSellerUseCase;
    }

    @PostMapping
    public SellerResponse register(ActorContext actor, @Valid @RequestBody CreateSellerRequest request) {
        return SellerWebMapper.toResponse(
                registerSellerUseCase.registerSeller(actor, SellerWebMapper.toCommand(request)));
    }

    @GetMapping("/{sellerId}")
    public SellerResponse find(ActorContext actor, @PathVariable Long sellerId) {
        return SellerWebMapper.toResponse(registerSellerUseCase.findSeller(actor, sellerId));
    }
}
