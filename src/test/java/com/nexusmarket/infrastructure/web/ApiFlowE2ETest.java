package com.nexusmarket.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end flows over the real REST edge, application services and JPA
 * adapters against H2 (profile {@code test}).
 *
 * <p>Fase 2.8 of docs/PLAN_API_WEB.md. There is no API to register a buyer, so
 * the buyer fixture is inserted through the output ports in the test setup.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowE2ETest {

    private static final String ACTOR_ID = "X-Actor-Id";
    private static final String ACTOR_ROLE = "X-Actor-Role";

    private final ObjectMapper json = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Test
    void sellerWarehouseProductAndStockAreRegistered() throws Exception {
        long sellerId = registerSeller("maria");
        long userId = sellerUserId(sellerId);

        long warehouseId = bodyOf(mockMvc.perform(post("/api/warehouses")
                        .header(ACTOR_ID, "1").header(ACTOR_ROLE, "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SELLER\",\"sellerId\":" + sellerId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SELLER"))
                .andReturn()).get("id").asLong();

        long productId = bodyOf(mockMvc.perform(post("/api/products")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\",\"variants\":[\"M\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(post("/api/inventories")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "SELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"warehouseId\":" + warehouseId
                                + ",\"quantity\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.productId").value(productId));
    }

    @Test
    void warehouseWithSellerIdAndMarketplaceTypeIsRejected() throws Exception {
        long sellerId = registerSeller("nulo");

        mockMvc.perform(post("/api/warehouses")
                        .header(ACTOR_ID, "1").header(ACTOR_ROLE, "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"MARKETPLACE\",\"sellerId\":" + sellerId + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingActorHeadersAreRejected() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void orderIsCreatedReadAndAdvancedThroughItsLifecycle() throws Exception {
        long userId = createBuyer("carlos");

        long orderId = bodyOf(mockMvc.perform(post("/api/orders")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CART"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(get("/api/orders/" + orderId)
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CART"));

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDING_PAYMENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }

    @Test
    void anInvalidOrderTransitionReturnsConflict() throws Exception {
        long userId = createBuyer("diana");
        long orderId = bodyOf(mockMvc.perform(post("/api/orders")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void anUnknownOrderReturnsNotFound() throws Exception {
        long userId = createBuyer("elena");

        mockMvc.perform(get("/api/orders/999999")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER"))
                .andExpect(status().isNotFound());
    }

    @Test
    void refundIsRequestedForAnExistentOrder() throws Exception {
        long userId = createBuyer("felipe");
        long orderId = bodyOf(mockMvc.perform(post("/api/orders")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(post("/api/refunds")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":" + orderId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));
    }

    @Test
    void refundForAnUnknownOrderReturnsNotFound() throws Exception {
        long userId = createBuyer("gabriel");

        mockMvc.perform(post("/api/refunds")
                        .header(ACTOR_ID, userId).header(ACTOR_ROLE, "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":999999}"))
                .andExpect(status().isNotFound());
    }

    private long registerSeller(String name) throws Exception {
        String unique = name + System.nanoTime();
        JsonNode body = bodyOf(mockMvc.perform(post("/api/sellers")
                        .header(ACTOR_ID, "1").header(ACTOR_ROLE, "ADMINISTRATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + unique
                                + "@nexusmarket.com\",\"identityDocument\":\"" + unique + "\"}"))
                .andExpect(status().isOk())
                .andReturn());
        return body.get("id").asLong();
    }

    private long sellerUserId(long sellerId) throws Exception {
        return bodyOf(mockMvc.perform(get("/api/sellers/" + sellerId)
                        .header(ACTOR_ID, "1").header(ACTOR_ROLE, "ADMINISTRATOR"))
                .andReturn()).get("userId").asLong();
    }

    private long createBuyer(String name) {
        String unique = name + System.nanoTime();
        User user = new User();
        user.setFullName(name);
        user.setEmail(unique + "@nexusmarket.com");
        user.setIdentityDocument(unique);
        user.setRole(Role.BUYER);
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);

        Buyer buyer = new Buyer();
        buyer.setUserId(saved.getId());
        buyer.setPrimaryAddress("Calle 1 # 2-3");
        buyer.setCommercialStatus("ACTIVE");
        buyerRepository.save(buyer);
        return saved.getId();
    }

    private JsonNode bodyOf(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }
}
