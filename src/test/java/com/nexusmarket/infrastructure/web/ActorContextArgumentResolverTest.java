package com.nexusmarket.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.domain.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

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

    private NativeWebRequest request(String actorId, String role) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        if (actorId != null) {
            servletRequest.addHeader(ActorContextArgumentResolver.HEADER_ACTOR_ID, actorId);
        }
        if (role != null) {
            servletRequest.addHeader(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, role);
        }
        return new ServletWebRequest(servletRequest);
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
    void buildsTheActorContextFromTheProvisionalHeaders() throws Exception {
        Object resolved = resolver.resolveArgument(
                parameter("withActor", ActorContext.class), null, request("10", "seller"), null);

        assertThat(resolved).isEqualTo(new ActorContext(10L, Role.SELLER));
    }

    @Test
    void rejectsAMissingHeader() throws Exception {
        assertThatThrownBy(() -> resolver.resolveArgument(
                parameter("withActor", ActorContext.class), null, request(null, "SELLER"), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsAnUnknownRole() throws Exception {
        assertThatThrownBy(() -> resolver.resolveArgument(
                parameter("withActor", ActorContext.class), null, request("10", "WIZARD"), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
