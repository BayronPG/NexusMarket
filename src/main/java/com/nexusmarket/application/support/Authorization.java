package com.nexusmarket.application.support;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.domain.user.Role;

/**
 * Role based authorization checks shared by the application services.
 *
 * <p>Every operation must be executed by an authenticated user (RG-01) and no
 * participant may administer information outside its role (RG-03). The allowed
 * roles per use case come from the explicit responsibilities matrix of the
 * specification.</p>
 *
 * <p>Traceability: RG-01, RG-03.</p>
 */
public final class Authorization {

    private Authorization() {
    }

    /**
     * Requires an authenticated actor.
     *
     * @param actor actor that executes the use case
     * @throws AuthorizationException if the actor is missing (RG-01)
     */
    public static void requireAuthenticated(ActorContext actor) {
        if (actor == null) {
            throw new AuthorizationException("An authenticated user is required (RG-01)");
        }
    }

    /**
     * Requires an authenticated actor holding one of the allowed roles.
     *
     * @param actor   actor that executes the use case
     * @param allowed roles explicitly allowed by the source
     * @throws AuthorizationException if the actor is missing or its role is not allowed
     */
    public static void requireAnyRole(ActorContext actor, Role... allowed) {
        requireAuthenticated(actor);
        for (Role role : allowed) {
            if (actor.role() == role) {
                return;
            }
        }
        throw new AuthorizationException(
                "Role " + actor.role() + " is not allowed for this operation (RG-03)");
    }
}
