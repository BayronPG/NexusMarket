package com.nexusmarket.infrastructure.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageWarehousesUseCase;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
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
 * MockMvc tests of {@link WarehouseController}.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@WebMvcTest(WarehouseController.class)
@Import({WebMvcConfig.class, ActorContextArgumentResolver.class})
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageWarehousesUseCase manageWarehousesUseCase;

    private Warehouse warehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(3L);
        warehouse.setType(WarehouseType.SELLER);
        warehouse.setSellerId(4L);
        return warehouse;
    }

    private Warehouse marketplaceWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(5L);
        warehouse.setType(WarehouseType.MARKETPLACE);
        warehouse.setSellerId(null);
        return warehouse;
    }

    @Test
    void registerDelegatesToTheUseCaseAndReturnsTheDto() throws Exception {
        given(manageWarehousesUseCase.registerWarehouse(
                new ActorContext(1L, Role.ADMINISTRATOR),
                new ManageWarehousesUseCase.RegisterCommand(WarehouseType.SELLER, 4L)))
                .willReturn(warehouse());

        mockMvc.perform(post("/api/warehouses")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SELLER\",\"sellerId\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.type").value("SELLER"))
                .andExpect(jsonPath("$.sellerId").value(4));
    }

    @Test
    void findBySellerDelegatesToTheUseCaseAndReturnsAList() throws Exception {
        given(manageWarehousesUseCase.findWarehousesBySeller(new ActorContext(4L, Role.SELLER), 4L))
                .willReturn(List.of(warehouse()));

        mockMvc.perform(get("/api/warehouses")
                        .param("sellerId", "4")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "4")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "SELLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].sellerId").value(4));
    }

    @Test
    void registerRejectsAMissingSellerIdWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/warehouses")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SELLER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerAcceptsAMarketplaceWarehouseWithoutSellerId() throws Exception {
        given(manageWarehousesUseCase.registerWarehouse(
                new ActorContext(1L, Role.ADMINISTRATOR),
                new ManageWarehousesUseCase.RegisterCommand(WarehouseType.MARKETPLACE, null)))
                .willReturn(marketplaceWarehouse());

        mockMvc.perform(post("/api/warehouses")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"MARKETPLACE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.type").value("MARKETPLACE"))
                .andExpect(jsonPath("$.sellerId").isEmpty());
    }

    @Test
    void registerRejectsAMarketplaceWarehouseWithSellerId() throws Exception {
        mockMvc.perform(post("/api/warehouses")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ID, "1")
                        .header(ActorContextArgumentResolver.HEADER_ACTOR_ROLE, "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"MARKETPLACE\",\"sellerId\":4}"))
                .andExpect(status().isBadRequest());
    }
}
