package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageOrdersUseCase;
import com.nexusmarket.infrastructure.web.dto.ChangeOrderStatusRequest;
import com.nexusmarket.infrastructure.web.dto.OrderResponse;
import com.nexusmarket.infrastructure.web.mapper.OrderWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md. OpenAPI documentation: Fase 2.6.</p>
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Ciclo del pedido (OBJ-07, OBJ-08, RN-13, RN-16)")
public class OrderController {

    private final ManageOrdersUseCase manageOrdersUseCase;

    public OrderController(ManageOrdersUseCase manageOrdersUseCase) {
        this.manageOrdersUseCase = manageOrdersUseCase;
    }

    @PostMapping
    @Operation(summary = "Crea un pedido en estado CART")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido creado"),
            @ApiResponse(responseCode = "400", description = "Cabeceras de actor invalidas"),
            @ApiResponse(responseCode = "403", description = "Solo un comprador crea pedidos"),
            @ApiResponse(responseCode = "404", description = "El comprador no existe")})
    public OrderResponse create(@Parameter(hidden = true) ActorContext actor) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.createOrder(actor));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Cambia el estado de un pedido")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas"),
            @ApiResponse(responseCode = "403", description = "El actor no puede operar este pedido (RG-03)"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe"),
            @ApiResponse(responseCode = "409", description = "Transicion no permitida (RN-16) o pedido finalizado (RN-13)")})
    public OrderResponse changeStatus(
            @Parameter(hidden = true) ActorContext actor,
            @PathVariable Long orderId,
            @Valid @RequestBody ChangeOrderStatusRequest request) {
        return OrderWebMapper.toResponse(
                manageOrdersUseCase.changeOrderStatus(actor, orderId, request.status()));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Consulta un pedido")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "400", description = "Cabeceras de actor invalidas"),
            @ApiResponse(responseCode = "403", description = "El rol no puede consultar pedidos"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")})
    public OrderResponse find(@Parameter(hidden = true) ActorContext actor,
                              @PathVariable Long orderId) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.findOrder(actor, orderId));
    }
}
