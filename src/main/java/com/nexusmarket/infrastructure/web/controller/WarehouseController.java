package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageWarehousesUseCase;
import com.nexusmarket.infrastructure.web.dto.CreateWarehouseRequest;
import com.nexusmarket.infrastructure.web.dto.WarehouseResponse;
import com.nexusmarket.infrastructure.web.mapper.WarehouseWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md. OpenAPI documentation: Fase 2.6.</p>
 */
@RestController
@RequestMapping("/api/warehouses")
@Tag(name = "Warehouses", description = "Bodegas de vendedor y del marketplace (OBJ-05)")
public class WarehouseController {

    private final ManageWarehousesUseCase manageWarehousesUseCase;

    public WarehouseController(ManageWarehousesUseCase manageWarehousesUseCase) {
        this.manageWarehousesUseCase = manageWarehousesUseCase;
    }

    @PostMapping
    @Operation(summary = "Registra una bodega")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bodega registrada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas (sellerId incoherente con el tipo)"),
            @ApiResponse(responseCode = "403", description = "El rol no puede registrar bodegas"),
            @ApiResponse(responseCode = "404", description = "El vendedor no existe")})
    public WarehouseResponse register(@Parameter(hidden = true) ActorContext actor,
                                      @Valid @RequestBody CreateWarehouseRequest request) {
        return WarehouseWebMapper.toResponse(
                manageWarehousesUseCase.registerWarehouse(actor, WarehouseWebMapper.toCommand(request)));
    }

    @GetMapping
    @Operation(summary = "Lista las bodegas de un vendedor")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR"),
            @Parameter(name = "sellerId", in = ParameterIn.QUERY, required = true,
                    description = "Identificador del vendedor")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bodegas del vendedor"),
            @ApiResponse(responseCode = "400", description = "Cabeceras o parametro invalidos"),
            @ApiResponse(responseCode = "403", description = "El rol no puede consultar bodegas")})
    public List<WarehouseResponse> findBySeller(@Parameter(hidden = true) ActorContext actor,
                                                @RequestParam Long sellerId) {
        return WarehouseWebMapper.toResponses(
                manageWarehousesUseCase.findWarehousesBySeller(actor, sellerId));
    }
}
