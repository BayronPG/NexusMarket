package com.nexusmarket.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.domain.user.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tests of {@link ActorContextArgumentResolver}.
 *
 * <p>Fase 2.5 of docs/PLAN_API_WEB.md.</p>
 */
class ActorContextArgumentResolverTest {

    private final ActorContextArgumentResolver resolver = new ActorContextArgumentResolver();

    private static class SampleController {

        void withActor(ActorContext actor) {
        }

        void withOther(Long value) {
        }
    }

    private MethodParameter parameter(String method, Class<?> type) throws NoSuchMethodException {
        return new MethodParameter(SampleController.class.getDeclaredMethod(method, type), 0);
    }

    @Test
    void supportsTheActorContextParameter() throws Exception {
        assertThat(resolver.supportsParameter(parameter("withActor", ActorContext.class))).isTrue();
    }

    @Test
    void doesNotSupportOtherParameters() throws Exception {
        assertThat(resolver.supportsParameter(parameter("withOther", Long.class))).isFalse();
    }

    @Test
    void buildsTheActorContextFromJwt() throws Exception {
        authenticate("25", "SELLER");
        Object resolved = resolver.resolveArgument(
                parameter("withActor", ActorContext.class), null, null, null);

        assertThat(resolved).isEqualTo(new ActorContext(25L, Role.SELLER));
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsMissingJwt() throws Exception {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> resolver.resolveArgument(
                parameter("withActor", ActorContext.class), null, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    @Test
    void rejectsInvalidSubject() throws Exception {
        authenticate("not-a-number", "SELLER");
        assertThatThrownBy(() -> resolver.resolveArgument(
                parameter("withActor", ActorContext.class), null, null, null))
                .isInstanceOf(ResponseStatusException.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsUnknownRole() throws Exception {
        authenticate("25", "WIZARD");
        assertThatThrownBy(() -> resolver.resolveArgument(
                parameter("withActor", ActorContext.class), null, null, null))
                .isInstanceOf(ResponseStatusException.class);
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String subject, String role) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(subject)
                .claim("role", role)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
