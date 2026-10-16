package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageCatalogUseCase;
import com.nexusmarket.infrastructure.web.dto.ChangeProductStatusRequest;
import com.nexusmarket.infrastructure.web.dto.CreateProductRequest;
import com.nexusmarket.infrastructure.web.dto.ProductResponse;
import com.nexusmarket.infrastructure.web.mapper.ProductWebMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link ManageCatalogUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@RestController
@RequestMapping("/api/products")
public class CatalogController {

    private final ManageCatalogUseCase manageCatalogUseCase;

    public CatalogController(ManageCatalogUseCase manageCatalogUseCase) {
        this.manageCatalogUseCase = manageCatalogUseCase;
    }

    @PostMapping
    public ProductResponse register(ActorContext actor, @Valid @RequestBody CreateProductRequest request) {
        return ProductWebMapper.toResponse(
                manageCatalogUseCase.registerProduct(actor, ProductWebMapper.toCommand(request)));
    }

    @PatchMapping("/{productId}/status")
    public ProductResponse changeStatus(
            ActorContext actor,
            @PathVariable Long productId,
            @Valid @RequestBody ChangeProductStatusRequest request) {
        return ProductWebMapper.toResponse(
                manageCatalogUseCase.changeProductStatus(actor, productId, request.status()));
    }

    @GetMapping("/published")
    public List<ProductResponse> findPublished(ActorContext actor) {
        return ProductWebMapper.toResponses(manageCatalogUseCase.findPublishedProducts(actor));
    }

    @GetMapping("/mine")
    public List<ProductResponse> findOwn(ActorContext actor) {
        return ProductWebMapper.toResponses(manageCatalogUseCase.findOwnProducts(actor));
    }
}
