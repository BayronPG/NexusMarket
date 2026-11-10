package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.application.port.in.ManageCatalogUseCase;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.web.ActorContextArgumentResolver;
import com.nexusmarket.infrastructure.web.WebMvcConfig;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc tests of {@link CatalogController}.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@WebMvcTest(CatalogController.class)
@ActiveProfiles("test")
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class, SecurityConfig.class})
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageCatalogUseCase manageCatalogUseCase;

    private Product product(ProductStatus status) {
        Product product = new Product();
        product.setId(1L);
        product.setSellerId(2L);
        product.setType(ProductType.PHYSICAL);
        product.setVariants(List.of("Color: negro"));
        product.setStatus(status);
        return product;
    }

    @Test
    void registerDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageCatalogUseCase.registerProduct(
                new ActorContext(2L, Role.SELLER),
                new ManageCatalogUseCase.RegisterProductCommand(
                        ProductType.PHYSICAL, ProductStatus.PUBLISHED, List.of("Color: negro"))))
                .willReturn(product(ProductStatus.PUBLISHED));

        mockMvc.perform(post("/api/products")
                        .with(SecurityTestSupport.jwt("2", Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\","
                                + "\"variants\":[\"Color: negro\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("PHYSICAL"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void changeStatusDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageCatalogUseCase.changeProductStatus(
                new ActorContext(2L, Role.SELLER), 1L, ProductStatus.SUSPENDED))
                .willReturn(product(ProductStatus.SUSPENDED));

        mockMvc.perform(patch("/api/products/1/status")
                        .with(SecurityTestSupport.jwt("2", Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void findPublishedDelegatesToTheUseCaseAndReturnsAPage() throws Exception {
        given(manageCatalogUseCase.findPublishedProducts(new ActorContext(9L, Role.BUYER), null,
                PageQuery.of(0, 20, "id,asc")))
                .willReturn(PageResult.of(List.of(product(ProductStatus.PUBLISHED)), 0, 20, 1));

        mockMvc.perform(get("/api/products/published")
                        .with(SecurityTestSupport.jwt("9", Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].sellerId").value(2))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void findOwnDelegatesToTheUseCaseAndReturnsAPage() throws Exception {
        given(manageCatalogUseCase.findOwnProducts(new ActorContext(2L, Role.SELLER), null,
                PageQuery.of(0, 20, "id,asc")))
                .willReturn(PageResult.of(List.of(product(ProductStatus.PUBLISHED)), 0, 20, 1));

        mockMvc.perform(get("/api/products/mine")
                        .with(SecurityTestSupport.jwt("2", Role.SELLER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void registerRejectsAMissingStatusWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(SecurityTestSupport.jwt("2", Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\"}"))
                .andExpect(status().isBadRequest());
    }
}
