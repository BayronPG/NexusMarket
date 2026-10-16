package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageRefundsUseCase;
import com.nexusmarket.domain.returns.Refund;
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
 * MockMvc tests of {@link RefundController}.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@WebMvcTest(RefundController.class)
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class})
class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageRefundsUseCase manageRefundsUseCase;

    @Test
    void requestDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        Refund refund = new Refund();
        refund.setId(1L);
        refund.setOrderId(8L);

        given(manageRefundsUseCase.requestRefund(
                new ActorContext(6L, Role.BUYER),
                new ManageRefundsUseCase.RequestRefundCommand(8L)))
                .willReturn(refund);

        mockMvc.perform(post("/api/refunds")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "6")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(8));
    }

    @Test
    void requestRejectsAMissingOrderIdWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/refunds")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "6")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
