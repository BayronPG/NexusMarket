package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.user.Role;

/**
 * Output port that issues signed access tokens.
 *
 * <p>Traceability: authentication increment (login).</p>
 */
public interface TokenIssuer {

    /**
     * Result of issuing a token.
     *
     * @param accessToken      signed JWT
     * @param expiresInSeconds validity of the token in seconds
     */
    record IssuedToken(String accessToken, long expiresInSeconds) {
    }

    /**
     * Issues a token for the user.
     *
     * @param userId positive user identifier
     * @param role   exact role of the user
     * @return the signed token and its lifetime
     */
    IssuedToken issue(Long userId, Role role);
}
