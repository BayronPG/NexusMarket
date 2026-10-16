package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageOrdersUseCase;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
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
 * MockMvc tests of {@link OrderController}.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@WebMvcTest(OrderController.class)
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageOrdersUseCase manageOrdersUseCase;

    private Order order(OrderStatus status) {
        return new Order(5L, 6L, status);
    }

    @Test
    void createDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageOrdersUseCase.createOrder(new ActorContext(6L, Role.BUYER)))
                .willReturn(order(OrderStatus.CART));

        mockMvc.perform(post("/api/orders")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "6")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("CART"));
    }

    @Test
    void changeStatusDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageOrdersUseCase.changeOrderStatus(
                new ActorContext(6L, Role.BUYER), 5L, OrderStatus.PAID))
                .willReturn(order(OrderStatus.PAID));

        mockMvc.perform(patch("/api/orders/5/status")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "6")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void findDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageOrdersUseCase.findOrder(new ActorContext(6L, Role.BUYER), 5L))
                .willReturn(order(OrderStatus.SHIPPED));

        mockMvc.perform(get("/api/orders/5")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "6")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.buyerId").value(6))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void changeStatusRejectsAMissingStatusWithBadRequest() throws Exception {
        mockMvc.perform(patch("/api/orders/5/status")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "6")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
