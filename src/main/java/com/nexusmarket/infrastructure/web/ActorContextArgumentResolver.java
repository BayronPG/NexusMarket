package com.nexusmarket.infrastructure.web;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.domain.user.Role;
import java.util.Locale;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * TEMPORARY resolution of the {@link ActorContext} from explicit HTTP headers.
 *
 * <p>Fase 2.5 of docs/PLAN_API_WEB.md (section 5). Spring Security is not part
 * of this evidence, so the controllers receive the actor from the
 * {@code X-Actor-Id} and {@code X-Actor-Role} headers.</p>
 *
 * <p><strong>This is NOT authentication.</strong> The caller declares its own
 * identity and nothing verifies it: the value object is built as-is, which is
 * consistent with the declared limitation D-019 (RG-01 assumed, not verified).
 * The real authentication belongs to the security evidence.</p>
 */
@Component
public class ActorContextArgumentResolver implements HandlerMethodArgumentResolver {

    /** Provisional header that carries the identifier of the user. */
    public static final String HEADER_ACTOR_ID = "X-Actor-Id";

    /** Provisional header that carries the single role of the user (RG-02). */
    public static final String HEADER_ACTOR_ROLE = "X-Actor-Role";

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
        String rawId = webRequest.getHeader(HEADER_ACTOR_ID);
        String rawRole = webRequest.getHeader(HEADER_ACTOR_ROLE);
        if (rawId == null || rawId.isBlank() || rawRole == null || rawRole.isBlank()) {
            throw new IllegalStateException(
                    HEADER_ACTOR_ID + " and " + HEADER_ACTOR_ROLE + " headers are required (RG-01)");
        }
        // The HTTP status for a malformed header is deliberately deferred to the
        // global error handling of Fase 2.4.
        return new ActorContext(Long.valueOf(rawId.trim()), parseRole(rawRole.trim()));
    }

    private Role parseRole(String rawRole) {
        try {
            return Role.valueOf(rawRole.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(HEADER_ACTOR_ROLE + " is not a known role: " + rawRole);
        }
    }
}
