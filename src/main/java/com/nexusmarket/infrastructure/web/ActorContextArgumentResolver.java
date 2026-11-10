package com.nexusmarket.infrastructure.web;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.domain.user.Role;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves the {@link ActorContext} from the authenticated JWT.
 *
 * <p>The resource server validates the token; this resolver maps its claims
 * into the application value object.</p>
 */
@Component
public class ActorContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return ActorContext.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw unauthorized("JWT authentication is required");
        }
        try {
            long userId = Long.parseLong(jwt.getSubject());
            if (userId <= 0) {
                throw unauthorized("JWT subject must be a positive numeric ID");
            }
            return new ActorContext(userId, parseRole(jwt.getClaimAsString("role")));
        } catch (NumberFormatException | NullPointerException ex) {
            throw unauthorized("JWT subject must be a positive numeric ID");
        }
    }

    private Role parseRole(String rawRole) {
        try {
            // The decoder already rejected unknown roles; keep the claim exact
            // so a lowercase value can never be accepted by accident.
            return Role.valueOf(rawRole);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw unauthorized("JWT role is not a known role");
        }
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
