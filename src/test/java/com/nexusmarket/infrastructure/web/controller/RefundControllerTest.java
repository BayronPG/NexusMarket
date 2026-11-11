package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageRefundsUseCase;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.returns.RefundStatus;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import com.nexusmarket.infrastructure.web.ActorContextArgumentResolver;
import com.nexusmarket.infrastructure.web.WebMvcConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc tests of {@link RefundController}.
 *
 * <p>Traceability: OBJ-11.</p>
 */
@WebMvcTest(RefundController.class)
@ActiveProfiles("test")
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class, SecurityConfig.class})
class RefundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageRefundsUseCase manageRefundsUseCase;

    private static Refund refund() {
        return new Refund(1L, 7L, 8L, 9L, new BigDecimal("20.00"),
                RefundStatus.REGISTERED, LocalDateTime.of(2026, 11, 10, 18, 0));
    }

    @Test
    void findDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageRefundsUseCase.findRefund(new ActorContext(6L, Role.BUYER), 1L))
                .willReturn(refund());

        mockMvc.perform(get("/api/refunds/1")
                        .with(SecurityTestSupport.jwt("6", Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderItemId").value(9))
                .andExpect(jsonPath("$.amount").value(20.00));
    }

    @Test
    void findByOrderReturnsThePageEnvelope() throws Exception {
        given(manageRefundsUseCase.findRefundsByOrder(
                new ActorContext(6L, Role.BUYER), 8L, new PageQuery(0, 20, "id,asc")))
                .willReturn(PageResult.of(List.of(refund()), 0, 20, 1));

        mockMvc.perform(get("/api/refunds")
                        .param("orderId", "8")
                        .with(SecurityTestSupport.jwt("6", Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].returnId").value(7))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
