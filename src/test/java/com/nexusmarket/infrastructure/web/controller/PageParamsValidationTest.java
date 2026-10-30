package com.nexusmarket.infrastructure.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.port.in.ManageCatalogUseCase;
import com.nexusmarket.infrastructure.web.ActorContextArgumentResolver;
import com.nexusmarket.infrastructure.web.WebMvcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Bean Validation of the pagination parameters at the REST edge.
 *
 * <p>Fase 2.9 of docs/PLAN_API_WEB.md: invalid {@code page}, {@code size} or
 * {@code sort} produce 400 before the use case is invoked.</p>
 */
@WebMvcTest(CatalogController.class)
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class})
class PageParamsValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageCatalogUseCase manageCatalogUseCase;

    @Test
    void sizeAboveTheMaximumIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .param("size", "101")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "9")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sizeZeroIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .param("size", "0")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "9")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativePageIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .param("page", "-1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "9")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedSortIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .param("sort", "id;drop")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "9")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownSortFieldIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .param("sort", "password,asc")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "9")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER"))
                .andExpect(status().isBadRequest());
    }
}
