package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageCatalogUseCase;
import com.nexusmarket.infrastructure.web.dto.ChangeProductStatusRequest;
import com.nexusmarket.infrastructure.web.dto.CreateProductRequest;
import com.nexusmarket.infrastructure.web.dto.ProductResponse;
import com.nexusmarket.infrastructure.web.mapper.ProductWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.infrastructure.web.dto.PageResponse;
import com.nexusmarket.infrastructure.web.SortWhitelist;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
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
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md. OpenAPI documentation: Fase 2.6.</p>
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Catalogo de productos (OBJ-03, OBJ-04)")
@Validated
public class CatalogController {

    private static final Set<String> SORT_FIELDS = Set.of("id", "status", "type", "sellerId");

    private final ManageCatalogUseCase manageCatalogUseCase;

    public CatalogController(ManageCatalogUseCase manageCatalogUseCase) {
        this.manageCatalogUseCase = manageCatalogUseCase;
    }

    @PostMapping
    @Operation(summary = "Registra un producto")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto registrado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas"),
            @ApiResponse(responseCode = "403", description = "El rol no puede registrar productos"),
            @ApiResponse(responseCode = "404", description = "El vendedor no existe"),
            @ApiResponse(responseCode = "409", description = "Regla de negocio incumplida")})
    public ProductResponse register(@Parameter(hidden = true) ActorContext actor,
                                    @Valid @RequestBody CreateProductRequest request) {
        return ProductWebMapper.toResponse(
                manageCatalogUseCase.registerProduct(actor, ProductWebMapper.toCommand(request)));
    }

    @PatchMapping("/{productId}/status")
    @Operation(summary = "Cambia el estado de un producto")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas"),
            @ApiResponse(responseCode = "403", description = "El rol no puede cambiar el estado"),
            @ApiResponse(responseCode = "404", description = "El producto no existe"),
            @ApiResponse(responseCode = "409", description = "Transicion de estado no permitida")})
    public ProductResponse changeStatus(
            @Parameter(hidden = true) ActorContext actor,
            @PathVariable Long productId,
            @Valid @RequestBody ChangeProductStatusRequest request) {
        return ProductWebMapper.toResponse(
                manageCatalogUseCase.changeProductStatus(actor, productId, request.status()));
    }

    @GetMapping("/published")
    @Operation(summary = "Lista los productos publicados")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Productos publicados"),
            @ApiResponse(responseCode = "400", description = "Cabeceras de actor invalidas"),
            @ApiResponse(responseCode = "403", description = "El rol no puede consultar el catalogo")})
    public PageResponse<ProductResponse> findPublished(
            @Parameter(hidden = true) ActorContext actor,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc")
            @Pattern(regexp = "[A-Za-z][A-Za-z0-9]*(,(?i:asc|desc))?") String sort) {
        SortWhitelist.require(sort, SORT_FIELDS);
        return PageResponse.from(
                manageCatalogUseCase.findPublishedProducts(actor, sellerId,
                        PageQuery.of(page, size, sort)),
                ProductWebMapper::toResponse);
    }

    @GetMapping("/mine")
    @Operation(summary = "Lista los productos del vendedor autenticado")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Productos del vendedor"),
            @ApiResponse(responseCode = "400", description = "Cabeceras de actor invalidas"),
            @ApiResponse(responseCode = "403", description = "El rol no puede consultar sus productos"),
            @ApiResponse(responseCode = "404", description = "El vendedor no existe")})
    public PageResponse<ProductResponse> findOwn(
            @Parameter(hidden = true) ActorContext actor,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc")
            @Pattern(regexp = "[A-Za-z][A-Za-z0-9]*(,(?i:asc|desc))?") String sort) {
        SortWhitelist.require(sort, SORT_FIELDS);
        return PageResponse.from(
                manageCatalogUseCase.findOwnProducts(actor, status, PageQuery.of(page, size, sort)),
                ProductWebMapper::toResponse);
    }
}
