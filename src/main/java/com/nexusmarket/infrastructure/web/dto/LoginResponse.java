package com.nexusmarket.infrastructure.web.dto;

/**
 * Response body of {@code POST /api/auth/login}. Never contains the password
 * or its hash.
 *
 * @param accessToken signed JWT
 * @param tokenType   always {@code Bearer}
 * @param expiresIn   token lifetime in seconds
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
