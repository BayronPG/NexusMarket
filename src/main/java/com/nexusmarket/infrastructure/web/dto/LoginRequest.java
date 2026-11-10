package com.nexusmarket.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body of {@code POST /api/auth/login}.
 *
 * @param email    user email; normalized by the application service
 * @param password raw password
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
