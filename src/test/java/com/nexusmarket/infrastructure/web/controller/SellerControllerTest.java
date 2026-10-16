package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.RegisterSellerUseCase;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.web.ActorContextArgumentResolver;
import com.nexusmarket.infrastructure.web.WebMvcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc tests of {@link SellerController}.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@WebMvcTest(SellerController.class)
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class})
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterSellerUseCase registerSellerUseCase;

    private Seller seller() {
        Seller seller = new Seller();
        seller.setId(1L);
        seller.setUserId(10L);
        return seller;
    }

    @Test
    void registerDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(registerSellerUseCase.registerSeller(
                new ActorContext(10L, Role.SELLER),
                new RegisterSellerUseCase.Command("Ada Lovelace", "ada@nexus.test", "DOC-1")))
                .willReturn(seller());

        mockMvc.perform(post("/api/sellers")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "10")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Ada Lovelace\",\"email\":\"ada@nexus.test\","
                                + "\"identityDocument\":\"DOC-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    void findDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(registerSellerUseCase.findSeller(new ActorContext(10L, Role.SELLER), 1L))
                .willReturn(seller());

        mockMvc.perform(get("/api/sellers/1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "10")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "SELLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    void registerRejectsABlankFullNameWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/sellers")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "10")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"ada@nexus.test\","
                                + "\"identityDocument\":\"DOC-1\"}"))
                .andExpect(status().isBadRequest());
    }
}
