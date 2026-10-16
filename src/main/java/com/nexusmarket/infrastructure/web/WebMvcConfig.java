package com.nexusmarket.infrastructure.web;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the provisional {@link ActorContextArgumentResolver}.
 *
 * <p>Fase 2.5 of docs/PLAN_API_WEB.md. It only wires the resolver required by
 * Fase 2.1; no other MVC customisation is added.</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ActorContextArgumentResolver actorContextArgumentResolver;

    public WebMvcConfig(ActorContextArgumentResolver actorContextArgumentResolver) {
        this.actorContextArgumentResolver = actorContextArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(actorContextArgumentResolver);
    }
}
