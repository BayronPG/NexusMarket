package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageReturnsUseCase;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.infrastructure.web.SortWhitelist;
import com.nexusmarket.infrastructure.web.dto.CreateReturnRequest;
import com.nexusmarket.infrastructure.web.dto.PageResponse;
import com.nexusmarket.infrastructure.web.dto.ReturnResponse;
import com.nexusmarket.infrastructure.web.mapper.ReturnWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the returns use case.
 *
 * <p>Increment "envío y devolución por ítem" (2026-11-10): request, approve,
 * receive and query returns. Reception generates the partial refund.</p>
 *
 * <p>Traceability: OBJ-11.</p>
 */
@RestController
@RequestMapping("/api/returns")
@Tag(name = "Returns", description = "Devoluciones por linea y reembolso parcial (OBJ-11)")
@Validated
public class ReturnController {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "orderId", "orderItemId", "quantity", "status", "createdAt");

    private final ManageReturnsUseCase manageReturnsUseCase;

    public ReturnController(ManageReturnsUseCase manageReturnsUseCase) {
        this.manageReturnsUseCase = manageReturnsUseCase;
    }

    @PostMapping
    @Operation(summary = "El comprador solicita la devolucion de una linea entregada")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Devolucion registrada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo invalido o cantidad fuera de rango"),
            @ApiResponse(responseCode = "403", description = "Solo el comprador dueno del pedido"),
            @ApiResponse(responseCode = "404", description = "Pedido o linea inexistente"),
            @ApiResponse(responseCode = "409", description = "La linea no esta DELIVERED")})
    public ReturnResponse request(@Parameter(hidden = true) ActorContext actor,
                                  @Valid @RequestBody CreateReturnRequest request) {
        return ReturnWebMapper.toResponse(
                manageReturnsUseCase.requestReturn(actor, ReturnWebMapper.toCommand(request)), null);
    }

    @PatchMapping("/{returnId}/approve")
    @Operation(summary = "El administrador aprueba la devolucion")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Devolucion aprobada"),
            @ApiResponse(responseCode = "403", description = "Solo el administrador"),
            @ApiResponse(responseCode = "404", description = "Devolucion inexistente"),
            @ApiResponse(responseCode = "409", description = "La devolucion no esta REQUESTED")})
    public ReturnResponse approve(@Parameter(hidden = true) ActorContext actor,
                                  @PathVariable Long returnId) {
        return ReturnWebMapper.toResponse(
                manageReturnsUseCase.approveReturn(actor, returnId), null);
    }

    @PatchMapping("/{returnId}/receive")
    @Operation(summary = "El operador logistico recibe la devolucion y genera el reembolso")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Devolucion recibida y reembolso generado"),
            @ApiResponse(responseCode = "403", description = "Solo el operador logistico"),
            @ApiResponse(responseCode = "404", description = "Devolucion inexistente"),
            @ApiResponse(responseCode = "409", description = "La devolucion no esta APPROVED")})
    public ReturnResponse receive(@Parameter(hidden = true) ActorContext actor,
                                  @PathVariable Long returnId) {
        ManageReturnsUseCase.ReturnReceipt receipt =
                manageReturnsUseCase.receiveReturn(actor, returnId);
        return ReturnWebMapper.toResponse(receipt.returnRecord(), receipt.refund());
    }

    @GetMapping("/{returnId}")
    @Operation(summary = "Consulta una devolucion")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Devolucion encontrada"),
            @ApiResponse(responseCode = "403", description = "Solo el comprador dueno o el administrador"),
            @ApiResponse(responseCode = "404", description = "Devolucion inexistente")})
    public ReturnResponse find(@Parameter(hidden = true) ActorContext actor,
                               @PathVariable Long returnId) {
        return ReturnWebMapper.toResponse(
                manageReturnsUseCase.findReturn(actor, returnId), null);
    }

    @GetMapping
    @Operation(summary = "Lista las devoluciones de un pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido"),
            @ApiResponse(responseCode = "200", description = "Devoluciones del pedido"),
            @ApiResponse(responseCode = "403", description = "Solo el comprador dueno o el administrador"),
            @ApiResponse(responseCode = "404", description = "Pedido inexistente")})
    public PageResponse<ReturnResponse> findByOrder(
            @Parameter(hidden = true) ActorContext actor,
            @RequestParam Long orderId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc")
            @Pattern(regexp = "[A-Za-z][A-Za-z0-9]*(,(?i:asc|desc))?") String sort) {
        SortWhitelist.require(sort, SORT_FIELDS);
        return PageResponse.from(
                manageReturnsUseCase.findReturnsByOrder(actor, orderId,
                        PageQuery.of(page, size, sort)),
                returnRecord -> ReturnWebMapper.toResponse(returnRecord, null));
    }
}
