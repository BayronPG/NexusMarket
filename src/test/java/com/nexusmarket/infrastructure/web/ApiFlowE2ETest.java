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
import com.nexusmarket.infrastructure.web.controller.SecurityTestSupport;
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
                        .with(SecurityTestSupport.jwt("1", Role.ADMINISTRATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SELLER\",\"sellerId\":" + sellerId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SELLER"))
                .andReturn()).get("id").asLong();

        long productId = bodyOf(mockMvc.perform(post("/api/products")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\",\"variants\":[\"M\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn()).get("id").asLong();

        mockMvc.perform(post("/api/inventories")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.SELLER))
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
                        .with(SecurityTestSupport.jwt("1", Role.ADMINISTRATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"MARKETPLACE\",\"sellerId\":" + sellerId + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingJwtIsRejected() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anEmptyOrderCannotAdvanceToPendingPayment() throws Exception {
        long userId = createBuyer("carlos");

        long orderId = bodyOf(mockMvc.perform(post("/api/orders")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CART"))
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.total").value(0.00))
                .andReturn()).get("id").asLong();

        mockMvc.perform(get("/api/orders/" + orderId)
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CART"))
                .andExpect(jsonPath("$.items").isEmpty());

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDING_PAYMENT\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void cartItemsAreAddedPaidAndTheInventoryIsDeductedOnce() throws Exception {
        long sellerId = registerSeller("helena");
        long sellerUserId = sellerUserId(sellerId);
        long warehouseId = bodyOf(mockMvc.perform(post("/api/warehouses")
                        .with(SecurityTestSupport.jwt("1", Role.ADMINISTRATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SELLER\",\"sellerId\":" + sellerId + "}"))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();

        long productId = bodyOf(mockMvc.perform(post("/api/products")
                        .with(SecurityTestSupport.jwt(String.valueOf(sellerUserId), Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\",\"variants\":[],"
                                + "\"price\":25.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(25.50))
                .andReturn()).get("id").asLong();

        mockMvc.perform(post("/api/inventories")
                        .with(SecurityTestSupport.jwt(String.valueOf(sellerUserId), Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"warehouseId\":" + warehouseId
                                + ",\"quantity\":10}"))
                .andExpect(status().isOk());

        long buyerUserId = createBuyer("ivan");
        long orderId = bodyOf(mockMvc.perform(post("/api/orders")
                        .with(SecurityTestSupport.jwt(String.valueOf(buyerUserId), Role.BUYER)))
                .andReturn()).get("id").asLong();

        long itemId = bodyOf(mockMvc.perform(post("/api/orders/" + orderId + "/items")
                        .with(SecurityTestSupport.jwt(String.valueOf(buyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].unitPrice").value(25.50))
                .andExpect(jsonPath("$.items[0].subtotal").value(51.00))
                .andExpect(jsonPath("$.total").value(51.00))
                .andReturn()).get("items").get(0).get("id").asLong();

        mockMvc.perform(post("/api/orders/" + orderId + "/items")
                        .with(SecurityTestSupport.jwt(String.valueOf(buyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.total").value(76.50));

        mockMvc.perform(patch("/api/orders/" + orderId + "/items/" + itemId)
                        .with(SecurityTestSupport.jwt(String.valueOf(buyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(102.00));

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .with(SecurityTestSupport.jwt(String.valueOf(buyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PENDING_PAYMENT\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .with(SecurityTestSupport.jwt(String.valueOf(buyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .with(SecurityTestSupport.jwt(String.valueOf(buyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAID\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/inventories")
                        .param("productId", String.valueOf(productId))
                        .with(SecurityTestSupport.jwt(String.valueOf(sellerUserId), Role.SELLER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity").value(6));

        long otherBuyerUserId = createBuyer("julia");
        mockMvc.perform(post("/api/orders/" + orderId + "/items")
                        .with(SecurityTestSupport.jwt(String.valueOf(otherBuyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":1}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders/" + orderId + "/items")
                        .with(SecurityTestSupport.jwt(String.valueOf(sellerUserId), Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anInvalidOrderTransitionReturnsConflict() throws Exception {
        long userId = createBuyer("diana");
        long orderId = bodyOf(mockMvc.perform(post("/api/orders")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER)))
                .andReturn()).get("id").asLong();

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void anUnknownOrderReturnsNotFound() throws Exception {
        long userId = createBuyer("elena");

        mockMvc.perform(get("/api/orders/999999")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void refundIsRequestedForAnExistentOrder() throws Exception {
        long userId = createBuyer("felipe");
        long orderId = bodyOf(mockMvc.perform(post("/api/orders")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER)))
                .andReturn()).get("id").asLong();

        mockMvc.perform(post("/api/refunds")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":" + orderId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));
    }

    @Test
    void refundForAnUnknownOrderReturnsNotFound() throws Exception {
        long userId = createBuyer("gabriel");

        mockMvc.perform(post("/api/refunds")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishedProductsCanBePagedAndFilteredBySeller() throws Exception {
        long sellerId = registerSeller("helena");
        long userId = sellerUserId(sellerId);
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/products")
                            .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.SELLER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\",\"variants\":[]}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/products/published")
                        .param("sellerId", String.valueOf(sellerId))
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "id,desc")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void anInvalidPageSizeIsRejected() throws Exception {
        long userId = createBuyer("irene");

        mockMvc.perform(get("/api/products/published")
                        .param("size", "500")
                        .with(SecurityTestSupport.jwt(String.valueOf(userId), Role.BUYER)))
                .andExpect(status().isBadRequest());
    }

    private long registerSeller(String name) throws Exception {
        String unique = name + System.nanoTime();
        JsonNode body = bodyOf(mockMvc.perform(post("/api/sellers")
                        .with(SecurityTestSupport.jwt("1", Role.ADMINISTRATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + unique
                                + "@nexusmarket.com\",\"identityDocument\":\"" + unique + "\"}"))
                .andExpect(status().isOk())
                .andReturn());
        return body.get("id").asLong();
    }

    private long sellerUserId(long sellerId) throws Exception {
        return bodyOf(mockMvc.perform(get("/api/sellers/" + sellerId)
                        .with(SecurityTestSupport.jwt("1", Role.ADMINISTRATOR)))
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
