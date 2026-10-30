package com.nexusmarket.infrastructure.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal OpenAPI document metadata (Fase 2.6 of docs/PLAN_API_WEB.md).
 *
 * <p>Only the title, short description and API version are set here. The
 * individual endpoints, the provisional actor headers and the error responses
 * are documented in a later task.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusMarketOpenApi() {
        return new OpenAPI().info(new Info()
                .title("NexusMarket API")
                .description("API REST de NexusMarket: catalogo, inventarios, pedidos, "
                        + "devoluciones, vendedores y bodegas.")
                .version("0.1.0"));
    }
}
