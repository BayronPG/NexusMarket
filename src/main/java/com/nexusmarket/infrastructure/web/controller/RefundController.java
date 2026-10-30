package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageRefundsUseCase;
import com.nexusmarket.infrastructure.web.dto.CreateRefundRequest;
import com.nexusmarket.infrastructure.web.dto.RefundResponse;
import com.nexusmarket.infrastructure.web.mapper.RefundWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link ManageRefundsUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md. OpenAPI documentation: Fase 2.6.</p>
 */
@RestController
@RequestMapping("/api/refunds")
@Tag(name = "Refunds", description = "Solicitudes de reembolso (OBJ-11)")
public class RefundController {

    private final ManageRefundsUseCase manageRefundsUseCase;

    public RefundController(ManageRefundsUseCase manageRefundsUseCase) {
        this.manageRefundsUseCase = manageRefundsUseCase;
    }

    @PostMapping
    @Operation(summary = "Solicita un reembolso para un pedido")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reembolso registrado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas"),
            @ApiResponse(responseCode = "403", description = "El actor no puede solicitar el reembolso (RG-03)"),
            @ApiResponse(responseCode = "404", description = "El pedido o el comprador no existen")})
    public RefundResponse request(@Parameter(hidden = true) ActorContext actor,
                                  @Valid @RequestBody CreateRefundRequest request) {
        return RefundWebMapper.toResponse(
                manageRefundsUseCase.requestRefund(actor, RefundWebMapper.toCommand(request)));
    }
}
