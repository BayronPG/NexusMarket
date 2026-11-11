package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageOrdersUseCase;
import com.nexusmarket.infrastructure.web.dto.AddOrderItemRequest;
import com.nexusmarket.infrastructure.web.dto.ChangeOrderStatusRequest;
import com.nexusmarket.infrastructure.web.dto.OrderResponse;
import com.nexusmarket.infrastructure.web.dto.UpdateOrderItemRequest;
import com.nexusmarket.infrastructure.web.mapper.OrderWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md; the cart lines belong to the increment
 * "pedidos con items".</p>
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
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Pedido creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @ApiResponse(responseCode = "403", description = "Solo un comprador crea pedidos"),
            @ApiResponse(responseCode = "404", description = "El comprador no existe")})
    public OrderResponse create(@Parameter(hidden = true) ActorContext actor) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.createOrder(actor));
    }

    @PostMapping("/{orderId}/items")
    @Operation(summary = "Agrega un producto al carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Pedido actualizado con sus items"),
            @ApiResponse(responseCode = "400", description = "Cuerpo invalido (productId, quantity)"),
            @ApiResponse(responseCode = "403", description = "Solo el comprador propietario edita el carrito"),
            @ApiResponse(responseCode = "404", description = "Pedido o producto inexistente"),
            @ApiResponse(responseCode = "409", description = "Producto no publicado, sin precio o inventario insuficiente")})
    public OrderResponse addItem(
            @Parameter(hidden = true) ActorContext actor,
            @PathVariable Long orderId,
            @Valid @RequestBody AddOrderItemRequest request) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.addItem(
                actor, orderId, request.productId(), request.quantity()));
    }

    @PatchMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Cambia la cantidad de un item del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Pedido actualizado con sus items"),
            @ApiResponse(responseCode = "400", description = "Cantidad invalida"),
            @ApiResponse(responseCode = "403", description = "Solo el comprador propietario edita el carrito"),
            @ApiResponse(responseCode = "404", description = "Pedido o item inexistente"),
            @ApiResponse(responseCode = "409", description = "El pedido no esta en CART o no hay inventario suficiente")})
    public OrderResponse updateItem(
            @Parameter(hidden = true) ActorContext actor,
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateOrderItemRequest request) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.updateItem(
                actor, orderId, itemId, request.quantity()));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Elimina un item del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Pedido actualizado sin el item"),
            @ApiResponse(responseCode = "403", description = "Solo el comprador propietario edita el carrito"),
            @ApiResponse(responseCode = "404", description = "Pedido o item inexistente"),
            @ApiResponse(responseCode = "409", description = "El pedido no esta en CART")})
    public OrderResponse removeItem(
            @Parameter(hidden = true) ActorContext actor,
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.removeItem(actor, orderId, itemId));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Cambia el estado de un pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo invalido"),
            @ApiResponse(responseCode = "403", description = "El actor no puede operar este pedido (RG-03)"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe"),
            @ApiResponse(responseCode = "409", description = "Transicion no permitida (RN-16), pedido sin items o inventario insuficiente")})
    public OrderResponse changeStatus(
            @Parameter(hidden = true) ActorContext actor,
            @PathVariable Long orderId,
            @Valid @RequestBody ChangeOrderStatusRequest request) {
        return OrderWebMapper.toResponse(
                manageOrdersUseCase.changeOrderStatus(actor, orderId, request.status()));
    }

    @PatchMapping("/{orderId}/items/{itemId}/ship")
    @Operation(summary = "El vendedor despacha su linea del pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Linea despachada"),
            @ApiResponse(responseCode = "403", description = "Solo el vendedor dueno del producto"),
            @ApiResponse(responseCode = "404", description = "Pedido, linea o producto inexistente"),
            @ApiResponse(responseCode = "409", description = "El pedido no esta pagado o la linea no esta PENDING")})
    public OrderResponse shipItem(@Parameter(hidden = true) ActorContext actor,
                                  @PathVariable Long orderId,
                                  @PathVariable Long itemId) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.shipItem(actor, orderId, itemId));
    }

    @PatchMapping("/{orderId}/items/{itemId}/deliver")
    @Operation(summary = "El operador logistico confirma la entrega de una linea")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Linea entregada"),
            @ApiResponse(responseCode = "403", description = "Solo el operador logistico"),
            @ApiResponse(responseCode = "404", description = "Pedido o linea inexistente"),
            @ApiResponse(responseCode = "409", description = "La linea no esta SHIPPED")})
    public OrderResponse deliverItem(@Parameter(hidden = true) ActorContext actor,
                                     @PathVariable Long orderId,
                                     @PathVariable Long itemId) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.deliverItem(actor, orderId, itemId));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Consulta un pedido con sus items y total")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @ApiResponse(responseCode = "403", description = "El rol no puede consultar pedidos"),
            @ApiResponse(responseCode = "404", description = "El pedido no existe")})
    public OrderResponse find(@Parameter(hidden = true) ActorContext actor,
                              @PathVariable Long orderId) {
        return OrderWebMapper.toResponse(manageOrdersUseCase.findOrder(actor, orderId));
    }
}
