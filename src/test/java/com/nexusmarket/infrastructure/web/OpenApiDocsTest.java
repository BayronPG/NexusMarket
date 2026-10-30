package com.nexusmarket.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Verifies that springdoc publishes the OpenAPI document and that the actor
 * headers documented in the controllers reach the generated paths.
 *
 * <p>Fase 2.6 of docs/PLAN_API_WEB.md.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsExposeTheDocumentedPathsAndActorHeaders() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("NexusMarket API"))
                .andExpect(jsonPath("$.paths['/api/products'].post").exists())
                .andExpect(jsonPath("$.paths['/api/orders/{orderId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/warehouses'].get").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("X-Actor-Id", "X-Actor-Role");
    }

    @Test
    void apiDocsExposeThePaginationParameters() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/products/published'].get.parameters[?(@.name=='page')]")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/products/published'].get.parameters[?(@.name=='size')]")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/products/published'].get.parameters[?(@.name=='sort')]")
                        .exists());
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
