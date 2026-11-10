package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageOrdersUseCase;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderItem;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.web.ActorContextArgumentResolver;
import com.nexusmarket.infrastructure.web.WebMvcConfig;
import com.nexusmarket.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;

/**
 * MockMvc tests of {@link OrderController}.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class, SecurityConfig.class})
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
                        .with(SecurityTestSupport.jwt("6", Role.BUYER)))
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
                        .with(SecurityTestSupport.jwt("6", Role.BUYER))
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
                        .with(SecurityTestSupport.jwt("6", Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.buyerId").value(6))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void changeStatusRejectsAMissingStatusWithBadRequest() throws Exception {
        mockMvc.perform(patch("/api/orders/5/status")
                        .with(SecurityTestSupport.jwt("6", Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemDelegatesToTheUseCaseAndReturnsTheLines() throws Exception {
        Order order = new Order(5L, 6L, OrderStatus.CART,
                List.of(new OrderItem(7L, 5L, 9L, 2, new BigDecimal("10.00"))));
        given(manageOrdersUseCase.addItem(new ActorContext(6L, Role.BUYER), 5L, 9L, 2))
                .willReturn(order);

        mockMvc.perform(post("/api/orders/5/items")
                        .with(SecurityTestSupport.jwt("6", Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":9,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(9))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].subtotal").value(20.00))
                .andExpect(jsonPath("$.total").value(20.00));
    }

    @Test
    void addItemRejectsANonPositiveQuantity() throws Exception {
        mockMvc.perform(post("/api/orders/5/items")
                        .with(SecurityTestSupport.jwt("6", Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":9,\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemDelegatesToTheUseCase() throws Exception {
        Order order = new Order(5L, 6L, OrderStatus.CART,
                List.of(new OrderItem(7L, 5L, 9L, 3, new BigDecimal("10.00"))));
        given(manageOrdersUseCase.updateItem(new ActorContext(6L, Role.BUYER), 5L, 7L, 3))
                .willReturn(order);

        mockMvc.perform(patch("/api/orders/5/items/7")
                        .with(SecurityTestSupport.jwt("6", Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    void removeItemDelegatesToTheUseCase() throws Exception {
        given(manageOrdersUseCase.removeItem(new ActorContext(6L, Role.BUYER), 5L, 7L))
                .willReturn(new Order(5L, 6L, OrderStatus.CART));

        mockMvc.perform(delete("/api/orders/5/items/7")
                        .with(SecurityTestSupport.jwt("6", Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0.00));
    }
}
