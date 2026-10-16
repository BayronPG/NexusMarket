package com.nexusmarket.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.InventoryRepository;
import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.application.port.out.ProductRepository;
import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.application.port.out.WarehouseRepository;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests of the persistence layer against a REAL MySQL database.
 *
 * <p>Fase 1.7 of docs/PLAN_API_WEB.md. H2 is deliberately not used here: this
 * class verifies the Flyway migrations, the schema that Hibernate validates and
 * the behaviour of the output-port adapters on the same engine as production.</p>
 *
 * <p>Requires a reachable MySQL instance and {@code DB_URL}, {@code DB_USER} and
 * {@code DB_PASSWORD}. It is excluded from the default build through the tag
 * {@code mysql}; run it with
 * {@code mvn -B test -Dtest=PersistenceMysqlIntegrationTest -DexcludedTestTags=none}.</p>
 */
@SpringBootTest
@ActiveProfiles("mysql")
@Transactional
@Tag("mysql")
class PersistenceMysqlIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private Environment environment;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Test
    void flywayAppliedTheSchemaMigrationsOnMysql() {
        assertThat(flyway.info().applied()).isNotEmpty();
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("1");
    }

    @Test
    void hibernateIsConfiguredToValidateTheSchemaInsteadOfUpdatingIt() {
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }

    @Test
    void userAdapterRoundTrip() {
        User user = new User();
        user.setFullName("Mysql User");
        user.setEmail("mysql.user@nexus.test");
        user.setIdentityDocument("MY-DOC-1");
        user.setRole(Role.SELLER);
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(User::getEmail, User::getRole)
                .containsExactly("mysql.user@nexus.test", Role.SELLER);
        assertThat(userRepository.findByEmail("mysql.user@nexus.test")).isPresent();
    }

    @Test
    void sellerAdapterRoundTrip() {
        Seller saved = sellerRepository.save(newSeller(1000L));

        assertThat(saved.getId()).isNotNull();
        assertThat(sellerRepository.findByUserId(1000L))
                .isPresent()
                .get()
                .extracting(Seller::getId)
                .isEqualTo(saved.getId());
    }

    @Test
    void buyerAdapterRoundTrip() {
        Buyer buyer = new Buyer();
        buyer.setUserId(2000L);
        buyer.setPrimaryAddress("Mysql address");
        buyer.setAdditionalAddresses(List.of("Mysql secondary"));
        buyer.setCommercialStatus("ACTIVE");

        Buyer saved = buyerRepository.save(buyer);

        assertThat(saved.getId()).isNotNull();
        assertThat(buyerRepository.findByUserId(2000L))
                .isPresent()
                .get()
                .extracting(Buyer::getAdditionalAddresses)
                .isEqualTo(List.of("Mysql secondary"));
    }

    @Test
    void warehouseAdapterRoundTrip() {
        Warehouse warehouse = new Warehouse();
        warehouse.setType(WarehouseType.SELLER);
        warehouse.setSellerId(3000L);

        Warehouse saved = warehouseRepository.save(warehouse);

        assertThat(saved.getId()).isNotNull();
        assertThat(warehouseRepository.findBySellerId(3000L)).hasSize(1);
    }

    @Test
    void productAdapterRoundTrip() {
        Product product = new Product();
        product.setSellerId(4000L);
        product.setType(ProductType.PHYSICAL);
        product.setVariants(List.of("Mysql variant"));
        product.setStatus(ProductStatus.PUBLISHED);

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findByStatus(ProductStatus.PUBLISHED))
                .anySatisfy(found -> assertThat(found.getId()).isEqualTo(saved.getId()));
    }

    @Test
    void inventoryAdapterRoundTrip() {
        Inventory inventory = new Inventory();
        inventory.setProductId(5000L);
        inventory.setWarehouseId(6000L);
        inventory.setQuantity(9);

        Inventory saved = inventoryRepository.save(inventory);

        assertThat(saved.getId()).isNotNull();
        assertThat(inventoryRepository.findByProductIdAndWarehouseId(5000L, 6000L))
                .isPresent()
                .get()
                .extracting(Inventory::getQuantity)
                .isEqualTo(9);
    }

    @Test
    void orderAdapterRoundTrip() {
        Order saved = orderRepository.save(new Order(null, 7000L, OrderStatus.CART));

        assertThat(saved.getId()).isNotNull();
        assertThat(orderRepository.findByBuyerId(7000L))
                .hasSize(1)
                .allSatisfy(order -> assertThat(order.getStatus()).isEqualTo(OrderStatus.CART));
    }

    @Test
    void refundAdapterRoundTrip() {
        Refund refund = new Refund();
        refund.setOrderId(8000L);

        Refund saved = refundRepository.save(refund);

        assertThat(saved.getId()).isNotNull();
        assertThat(refundRepository.findByOrderId(8000L))
                .hasSize(1)
                .allSatisfy(found -> assertThat(found.getOrderId()).isEqualTo(8000L));
    }

    private Seller newSeller(Long userId) {
        Seller seller = new Seller();
        seller.setUserId(userId);
        return seller;
    }
}
