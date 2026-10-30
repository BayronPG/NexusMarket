package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.RegisterSellerUseCase;
import com.nexusmarket.infrastructure.web.dto.CreateSellerRequest;
import com.nexusmarket.infrastructure.web.dto.SellerResponse;
import com.nexusmarket.infrastructure.web.mapper.SellerWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md. OpenAPI documentation: Fase 2.6. It depends
 * only on the input port, never on a concrete service or a JPA entity, and it
 * exposes DTOs instead of domain objects (section 3.2).</p>
 */
@RestController
@RequestMapping("/api/sellers")
@Tag(name = "Sellers", description = "Registro y consulta de vendedores (OBJ-02, RN-04)")
public class SellerController {

    private final RegisterSellerUseCase registerSellerUseCase;

    public SellerController(RegisterSellerUseCase registerSellerUseCase) {
        this.registerSellerUseCase = registerSellerUseCase;
    }

    @PostMapping
    @Operation(summary = "Registra un vendedor con su usuario")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vendedor registrado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo o cabeceras invalidas"),
            @ApiResponse(responseCode = "403", description = "Solo el Administrador registra vendedores (RN-04)"),
            @ApiResponse(responseCode = "409", description = "Email (RN-15) o documento (RN-14) ya registrados")})
    public SellerResponse register(@Parameter(hidden = true) ActorContext actor,
                                   @Valid @RequestBody CreateSellerRequest request) {
        return SellerWebMapper.toResponse(
                registerSellerUseCase.registerSeller(actor, SellerWebMapper.toCommand(request)));
    }

    @GetMapping("/{sellerId}")
    @Operation(summary = "Consulta un vendedor")
    @Parameters({
            @Parameter(name = "X-Actor-Id", in = ParameterIn.HEADER, required = true,
                    description = "Identificador del actor (provisional, no es autenticacion)"),
            @Parameter(name = "X-Actor-Role", in = ParameterIn.HEADER, required = true,
                    description = "Rol del actor: BUYER, SELLER, ADMINISTRATOR, LOGISTICS_OPERATOR")})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vendedor encontrado"),
            @ApiResponse(responseCode = "400", description = "Cabeceras de actor invalidas"),
            @ApiResponse(responseCode = "403", description = "Solo el Administrador consulta vendedores"),
            @ApiResponse(responseCode = "404", description = "El vendedor no existe")})
    public SellerResponse find(@Parameter(hidden = true) ActorContext actor,
                               @PathVariable Long sellerId) {
        return SellerWebMapper.toResponse(registerSellerUseCase.findSeller(actor, sellerId));
    }
}
