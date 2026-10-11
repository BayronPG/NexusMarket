package com.nexusmarket.application;

import com.nexusmarket.domain.user.Role;
import java.util.Objects;

/**
 * Authenticated actor that executes a use case.
 *
 * <p>EXPLICIT source: RG-01 "toda operación debe ser ejecutada por un usuario
 * autenticado". The concrete authentication mechanism belongs to a later
 * increment; this value object only carries the identity and the single role
 * of the actor (RG-02).</p>
 *
 * <p>Traceability: RG-01, RG-02, RG-03.</p>
 *
 * @param userId identifier of the authenticated user
 * @param role   the single role held by the user (RG-02)
 */
public record ActorContext(Long userId, Role role) {

    public ActorContext {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(role, "role is required");
    }
}
