package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.port.in.AuthenticateUserUseCase;
import com.nexusmarket.infrastructure.web.dto.LoginRequest;
import com.nexusmarket.infrastructure.web.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the authentication use case.
 *
 * <p>Only the login endpoint is public; every other endpoint remains protected
 * by the JWT resource server.</p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login con email y contrasena (JWT)")
@SecurityRequirements
public class AuthController {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica un usuario y emite un JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido"),
            @ApiResponse(responseCode = "400", description = "Cuerpo invalido"),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas (mensaje generico)")})
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthenticateUserUseCase.LoginResult result = authenticateUserUseCase.login(
                new AuthenticateUserUseCase.LoginCommand(request.email(), request.password()));
        return new LoginResponse(result.accessToken(), TOKEN_TYPE, result.expiresInSeconds());
    }
}
