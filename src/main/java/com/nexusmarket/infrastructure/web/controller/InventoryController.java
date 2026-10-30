package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageInventoryUseCase;
import com.nexusmarket.infrastructure.web.dto.InventoryResponse;
import com.nexusmarket.infrastructure.web.dto.RegisterStockRequest;
import com.nexusmarket.infrastructure.web.dto.ReserveStockRequest;
import com.nexusmarket.infrastructure.web.mapper.InventoryWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.infrastructure.web.dto.PageResponse;
import com.nexusmarket.infrastructure.web.SortWhitelist;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link ManageInventoryUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md. OpenAPI documentation: Fase 2.6.</p>
 */
@RestController
@RequestMapping("/api/inventories")
@Tag(name = "Inventories", description = "Inventario por producto y bodega (OBJ-06)")
@Validated
public class InventoryController {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "quantity", "damagedQuantity", "productId", "warehouseId");

    private final ManageInventoryUseCase manageInventoryUseCase;

    public InventoryController(ManageInventoryUseCase manageInventoryUseCase) {
        this.manageInventoryUseCase = manageInventoryUseCase;
    }

    @PostMapping
    @Operation(summary = "Registra stock de un producto en una bodega")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock registrado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas"),
            @ApiResponse(responseCode = "403", description = "El rol no puede registrar stock"),
            @ApiResponse(responseCode = "404", description = "El producto o la bodega no existen"),
            @ApiResponse(responseCode = "409", description = "El inventario ya existe para ese producto y bodega")})
    public InventoryResponse register(@Parameter(hidden = true) ActorContext actor,
                                      @Valid @RequestBody RegisterStockRequest request) {
        return InventoryWebMapper.toResponse(manageInventoryUseCase.registerStock(
                actor, InventoryWebMapper.toRegisterStockCommand(request)));
    }

    @PostMapping("/reservations")
    @Operation(summary = "Reserva stock de un producto")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock reservado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas"),
            @ApiResponse(responseCode = "403", description = "El rol no puede reservar stock"),
            @ApiResponse(responseCode = "404", description = "El inventario no existe"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente (RN)")})
    public InventoryResponse reserve(@Parameter(hidden = true) ActorContext actor,
                                     @Valid @RequestBody ReserveStockRequest request) {
        return InventoryWebMapper.toResponse(manageInventoryUseCase.reserveStock(
                actor, InventoryWebMapper.toReserveStockCommand(request)));
    }

    @GetMapping
    @Operation(summary = "Consulta el stock de un producto")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR"),
            @Parameter(name = "productId", in = ParameterIn.QUERY, required = true,
                    description = "Identificador del producto")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock del producto"),
            @ApiResponse(responseCode = "400", description = "Cabeceras o parametro invalidos"),
            @ApiResponse(responseCode = "403", description = "El rol no puede consultar el stock")})
    public PageResponse<InventoryResponse> findByProduct(
            @Parameter(hidden = true) ActorContext actor,
            @RequestParam Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc")
            @Pattern(regexp = "[A-Za-z][A-Za-z0-9]*(,(?i:asc|desc))?") String sort) {
        SortWhitelist.require(sort, SORT_FIELDS);
        return PageResponse.from(
                manageInventoryUseCase.findStockByProduct(actor, productId, warehouseId,
                        PageQuery.of(page, size, sort)),
                InventoryWebMapper::toResponse);
    }
}
