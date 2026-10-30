package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import com.nexusmarket.application.port.in.ManageInventoryUseCase;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.infrastructure.web.ActorContextArgumentResolver;
import com.nexusmarket.infrastructure.web.WebMvcConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc tests of {@link InventoryController}.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@WebMvcTest(InventoryController.class)
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class})
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageInventoryUseCase manageInventoryUseCase;

    private Inventory inventory() {
        Inventory inventory = new Inventory();
        inventory.setId(9L);
        inventory.setProductId(1L);
        inventory.setWarehouseId(2L);
        inventory.setQuantity(7);
        return inventory;
    }

    @Test
    void registerDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageInventoryUseCase.registerStock(
                new ActorContext(1L, Role.LOGISTICS_OPERATOR),
                new ManageInventoryUseCase.RegisterStockCommand(1L, 2L, 7)))
                .willReturn(inventory());

        mockMvc.perform(post("/api/inventories")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "LOGISTICS_OPERATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"warehouseId\":2,\"quantity\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.quantity").value(7));
    }

    @Test
    void reserveDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageInventoryUseCase.reserveStock(
                new ActorContext(1L, Role.LOGISTICS_OPERATOR),
                new ManageInventoryUseCase.ReserveStockCommand(1L, 2L, 3)))
                .willReturn(inventory());

        mockMvc.perform(post("/api/inventories/reservations")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "LOGISTICS_OPERATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"warehouseId\":2,\"amount\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.warehouseId").value(2));
    }

    @Test
    void findByProductDelegatesToTheUseCaseAndReturnsAPage() throws Exception {
        given(manageInventoryUseCase.findStockByProduct(new ActorContext(1L, Role.SUPERVISOR), 1L, null,
                PageQuery.of(0, 20, "id,asc")))
                .willReturn(PageResult.of(List.of(inventory()), 0, 20, 1));

        mockMvc.perform(get("/api/inventories")
                        .param("productId", "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "SUPERVISOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(9))
                .andExpect(jsonPath("$.content[0].quantity").value(7))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void registerRejectsAMissingProductIdWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/inventories")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "LOGISTICS_OPERATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseId\":2,\"quantity\":7}"))
                .andExpect(status().isBadRequest());
    }
}
