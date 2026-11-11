package com.nexusmarket.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import com.nexusmarket.infrastructure.web.controller.SecurityTestSupport;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Mandatory sustentation flow: one buyer purchases three products from three
 * different sellers in a single order, pays, each seller ships its own line,
 * the logistics operator delivers, the buyer returns one line and only that
 * line is refunded, with the inventory impact verified through GETs.
 *
 * <p>Traceability: OBJ-07, OBJ-08, OBJ-10, OBJ-11, RN-12, RN-13, RN-16, RG-03.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SustentationFlowE2ETest {

    private final ObjectMapper json = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void buyerPurchasesFromThreeSellersAndReturnsOnlyOneLine() throws Exception {
        Scenario scenario = scenario();
        RequestPostProcessor buyer = jwt(String.valueOf(scenario.buyerUserId()), Role.BUYER);
        RequestPostProcessor logistics = jwt("900", Role.LOGISTICS_OPERATOR);

        mockMvc.perform(get("/api/orders/" + scenario.orderId()).with(buyer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CART"))
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.items[0].sellerId").value(scenario.sellers().get(0).sellerId()))
                .andExpect(jsonPath("$.items[1].sellerId").value(scenario.sellers().get(1).sellerId()))
                .andExpect(jsonPath("$.items[2].sellerId").value(scenario.sellers().get(2).sellerId()))
                .andExpect(jsonPath("$.total").value(60.00));

        assertStock(scenario, 0, 10, 0);
        assertStock(scenario, 1, 10, 0);
        assertStock(scenario, 2, 10, 0);

        changeStatus(scenario, buyer, "PENDING_PAYMENT");
        changeStatus(scenario, buyer, "PAID");

        mockMvc.perform(get("/api/orders/" + scenario.orderId()).with(buyer))
                .andExpect(jsonPath("$.status").value("PAID"));
        assertStock(scenario, 0, 9, 0);
        assertStock(scenario, 1, 9, 0);
        assertStock(scenario, 2, 9, 0);

        // A seller cannot ship a line that belongs to another seller.
        ship(scenario, 0, 1, status().isForbidden());

        ship(scenario, 0, 0, status().isOk());
        ship(scenario, 1, 1, status().isOk());
        ship(scenario, 2, 2, status().isOk());

        mockMvc.perform(get("/api/orders/" + scenario.orderId()).with(buyer))
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.items[0].status").value("SHIPPED"))
                .andExpect(jsonPath("$.items[1].status").value("SHIPPED"))
                .andExpect(jsonPath("$.items[2].status").value("SHIPPED"));

        deliver(scenario, logistics, 0);
        deliver(scenario, logistics, 1);
        deliver(scenario, logistics, 2);

        mockMvc.perform(get("/api/orders/" + scenario.orderId()).with(buyer))
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.items[0].status").value("DELIVERED"))
                .andExpect(jsonPath("$.items[1].status").value("DELIVERED"))
                .andExpect(jsonPath("$.items[2].status").value("DELIVERED"));

        long returnId = requestReturn(scenario, buyer, 1, 1, "RESELLABLE");
        mockMvc.perform(get("/api/returns/" + returnId).with(buyer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.orderItemId").value(scenario.items().get(1)))
                .andExpect(jsonPath("$.quantity").value(1));

        mockMvc.perform(patch("/api/returns/" + returnId + "/approve")
                        .with(jwt("1", Role.ADMINISTRATOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        MvcResult received = mockMvc.perform(patch("/api/returns/" + returnId + "/receive")
                        .with(logistics))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.refundId").isNumber())
                .andReturn();
        long refundId = bodyOf(received).get("refundId").asLong();

        mockMvc.perform(get("/api/returns/" + returnId).with(buyer))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.refundId").value(refundId));

        mockMvc.perform(get("/api/refunds/" + refundId).with(buyer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId").value(scenario.items().get(1)))
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.status").value("REGISTERED"));

        mockMvc.perform(get("/api/refunds").param("orderId", String.valueOf(scenario.orderId()))
                        .with(buyer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].orderItemId").value(scenario.items().get(1)));

        mockMvc.perform(get("/api/orders/" + scenario.orderId()).with(buyer))
                .andExpect(jsonPath("$.items[0].status").value("DELIVERED"))
                .andExpect(jsonPath("$.items[1].status").value("REFUNDED"))
                .andExpect(jsonPath("$.items[2].status").value("DELIVERED"))
                .andExpect(jsonPath("$.total").value(60.00));

        assertStock(scenario, 0, 9, 0);
        assertStock(scenario, 1, 10, 0);
        assertStock(scenario, 2, 9, 0);
    }

    @Test
    void negativeRulesAreEnforced() throws Exception {
        Scenario scenario = scenario();
        RequestPostProcessor buyer = jwt(String.valueOf(scenario.buyerUserId()), Role.BUYER);
        RequestPostProcessor logistics = jwt("900", Role.LOGISTICS_OPERATOR);

        changeStatus(scenario, buyer, "PENDING_PAYMENT");
        changeStatus(scenario, buyer, "PAID");
        ship(scenario, 1, 1, status().isOk());

        // Return before delivery is rejected.
        mockMvc.perform(post("/api/returns").with(buyer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnBody(scenario, 1, 1, "RESELLABLE")))
                .andExpect(status().isConflict());

        deliver(scenario, logistics, 1);

        // Quantity greater than the purchased one is rejected.
        mockMvc.perform(post("/api/returns").with(buyer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnBody(scenario, 1, 2, "RESELLABLE")))
                .andExpect(status().isBadRequest());

        // A different buyer cannot read the order.
        long otherBuyer = createBuyer("other" + System.nanoTime());
        mockMvc.perform(get("/api/orders/" + scenario.orderId())
                        .with(jwt(String.valueOf(otherBuyer), Role.BUYER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void damagedReturnAndRepeatedReceptionAreIdempotent() throws Exception {
        Scenario scenario = scenario();
        RequestPostProcessor buyer = jwt(String.valueOf(scenario.buyerUserId()), Role.BUYER);
        RequestPostProcessor logistics = jwt("900", Role.LOGISTICS_OPERATOR);

        changeStatus(scenario, buyer, "PENDING_PAYMENT");
        changeStatus(scenario, buyer, "PAID");
        for (int i = 0; i < 3; i++) {
            ship(scenario, i, i, status().isOk());
        }
        for (int i = 0; i < 3; i++) {
            deliver(scenario, logistics, i);
        }

        // A later catalog price change must not alter the historical refund.
        Product product = productRepository.findById(scenario.sellers().get(1).productId())
                .orElseThrow();
        product.setPrice(new BigDecimal("999.00"));
        productRepository.save(product);

        long returnId = requestReturn(scenario, buyer, 1, 1, "DAMAGED");
        mockMvc.perform(patch("/api/returns/" + returnId + "/approve")
                        .with(jwt("1", Role.ADMINISTRATOR)))
                .andExpect(status().isOk());

        MvcResult first = mockMvc.perform(patch("/api/returns/" + returnId + "/receive")
                        .with(logistics))
                .andExpect(status().isOk())
                .andReturn();
        long refundId = bodyOf(first).get("refundId").asLong();

        // Repeating the reception returns the same refund and does not restock twice.
        mockMvc.perform(patch("/api/returns/" + returnId + "/receive").with(logistics))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundId").value(refundId));

        mockMvc.perform(get("/api/refunds/" + refundId).with(buyer))
                .andExpect(jsonPath("$.amount").value(20.00));

        assertStock(scenario, 1, 9, 1);
        mockMvc.perform(get("/api/refunds").param("orderId", String.valueOf(scenario.orderId()))
                        .with(buyer))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private Scenario scenario() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        long buyerUserId = createBuyer("buyer" + suffix);
        String[] prices = {"10.00", "20.00", "30.00"};
        List<SellerFixture> sellers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            long sellerId = registerSeller("seller" + i + suffix);
            long userId = sellerUserId(sellerId);
            long warehouseId = createWarehouse(sellerId);
            long productId = createProduct(userId, prices[i]);
            registerStock(userId, productId, warehouseId);
            sellers.add(new SellerFixture(sellerId, userId, warehouseId, productId));
        }
        long orderId = createOrder(buyerUserId);
        List<Long> items = new ArrayList<>();
        for (SellerFixture seller : sellers) {
            items.add(addItem(buyerUserId, orderId, seller.productId()));
        }
        return new Scenario(buyerUserId, orderId, List.copyOf(sellers), List.copyOf(items));
    }

    private long registerSeller(String name) throws Exception {
        String unique = name + System.nanoTime();
        return bodyOf(mockMvc.perform(post("/api/sellers")
                        .with(jwt("1", Role.ADMINISTRATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + unique
                                + "@nexus.test\",\"identityDocument\":\"CC-" + unique + "\"}"))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();
    }

    private long sellerUserId(long sellerId) throws Exception {
        return bodyOf(mockMvc.perform(get("/api/sellers/" + sellerId)
                        .with(jwt("1", Role.ADMINISTRATOR)))
                .andReturn()).get("userId").asLong();
    }

    private long createWarehouse(long sellerId) throws Exception {
        return bodyOf(mockMvc.perform(post("/api/warehouses")
                        .with(jwt("1", Role.ADMINISTRATOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SELLER\",\"sellerId\":" + sellerId + "}"))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();
    }

    private long createProduct(long sellerUserId, String price) throws Exception {
        return bodyOf(mockMvc.perform(post("/api/products")
                        .with(jwt(String.valueOf(sellerUserId), Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\",\"price\":"
                                + price + "}"))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();
    }

    private void registerStock(long sellerUserId, long productId, long warehouseId) throws Exception {
        mockMvc.perform(post("/api/inventories")
                        .with(jwt(String.valueOf(sellerUserId), Role.SELLER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"warehouseId\":" + warehouseId
                                + ",\"quantity\":10}"))
                .andExpect(status().isOk());
    }

    private long createOrder(long buyerUserId) throws Exception {
        return bodyOf(mockMvc.perform(post("/api/orders")
                        .with(jwt(String.valueOf(buyerUserId), Role.BUYER)))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();
    }

    private long addItem(long buyerUserId, long orderId, long productId) throws Exception {
        JsonNode body = bodyOf(mockMvc.perform(post("/api/orders/" + orderId + "/items")
                        .with(jwt(String.valueOf(buyerUserId), Role.BUYER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andReturn());
        for (JsonNode item : body.get("items")) {
            if (item.get("productId").asLong() == productId) {
                return item.get("id").asLong();
            }
        }
        throw new IllegalStateException("Item not found in the order response");
    }

    private void changeStatus(Scenario scenario, RequestPostProcessor buyer, String status)
            throws Exception {
        mockMvc.perform(patch("/api/orders/" + scenario.orderId() + "/status")
                        .with(buyer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk());
    }

    private void ship(Scenario scenario, int sellerIndex, int itemIndex,
                      org.springframework.test.web.servlet.ResultMatcher expected) throws Exception {
        mockMvc.perform(patch("/api/orders/" + scenario.orderId() + "/items/"
                        + scenario.items().get(itemIndex) + "/ship")
                        .with(jwt(String.valueOf(scenario.sellers().get(sellerIndex).userId()),
                                Role.SELLER)))
                .andExpect(expected);
    }

    private void deliver(Scenario scenario, RequestPostProcessor logistics, int itemIndex)
            throws Exception {
        mockMvc.perform(patch("/api/orders/" + scenario.orderId() + "/items/"
                        + scenario.items().get(itemIndex) + "/deliver")
                        .with(logistics))
                .andExpect(status().isOk());
    }

    private long requestReturn(Scenario scenario, RequestPostProcessor buyer, int itemIndex,
                               int quantity, String condition) throws Exception {
        return bodyOf(mockMvc.perform(post("/api/returns").with(buyer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnBody(scenario, itemIndex, quantity, condition)))
                .andExpect(status().isOk())
                .andReturn()).get("id").asLong();
    }

    private String returnBody(Scenario scenario, int itemIndex, int quantity, String condition) {
        return "{\"orderId\":" + scenario.orderId()
                + ",\"orderItemId\":" + scenario.items().get(itemIndex)
                + ",\"quantity\":" + quantity
                + ",\"reason\":\"Producto defectuoso\""
                + ",\"condition\":\"" + condition + "\"}";
    }

    private void assertStock(Scenario scenario, int index, int quantity, int damaged)
            throws Exception {
        SellerFixture seller = scenario.sellers().get(index);
        mockMvc.perform(get("/api/inventories")
                        .param("productId", String.valueOf(seller.productId()))
                        .with(jwt(String.valueOf(seller.userId()), Role.SELLER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity").value(quantity))
                .andExpect(jsonPath("$.content[0].damagedQuantity").value(damaged));
    }

    private long createBuyer(String name) {
        String unique = name + System.nanoTime();
        User user = new User();
        user.setFullName(name);
        user.setEmail(unique + "@nexus.test");
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

    private static RequestPostProcessor jwt(String subject, Role role) {
        return SecurityTestSupport.jwt(subject, role);
    }

    private record SellerFixture(long sellerId, long userId, long warehouseId, long productId) {
    }

    private record Scenario(long buyerUserId, long orderId, List<SellerFixture> sellers,
                            List<Long> items) {
    }
}
