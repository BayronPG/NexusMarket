package com.nexusmarket.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.application.port.in.ManageCatalogUseCase.RegisterProductCommand;
import com.nexusmarket.application.port.in.ManageInventoryUseCase.RegisterStockCommand;
import com.nexusmarket.application.port.in.ManageInventoryUseCase.ReserveStockCommand;
import com.nexusmarket.application.port.in.ManageRefundsUseCase.RequestRefundCommand;
import com.nexusmarket.application.port.in.ManageWarehousesUseCase;
import com.nexusmarket.application.port.in.RegisterSellerUseCase;
import com.nexusmarket.domain.catalog.Product;
import com.nexusmarket.domain.catalog.ProductStatus;
import com.nexusmarket.domain.catalog.ProductType;
import com.nexusmarket.domain.inventory.Inventory;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import com.nexusmarket.infrastructure.web.dto.ChangeProductStatusRequest;
import com.nexusmarket.infrastructure.web.dto.CreateProductRequest;
import com.nexusmarket.infrastructure.web.dto.CreateRefundRequest;
import com.nexusmarket.infrastructure.web.dto.CreateSellerRequest;
import com.nexusmarket.infrastructure.web.dto.CreateWarehouseRequest;
import com.nexusmarket.infrastructure.web.dto.InventoryResponse;
import com.nexusmarket.infrastructure.web.dto.OrderResponse;
import com.nexusmarket.infrastructure.web.dto.ProductResponse;
import com.nexusmarket.infrastructure.web.dto.RefundResponse;
import com.nexusmarket.infrastructure.web.dto.RegisterStockRequest;
import com.nexusmarket.infrastructure.web.dto.ReserveStockRequest;
import com.nexusmarket.infrastructure.web.dto.SellerResponse;
import com.nexusmarket.infrastructure.web.dto.WarehouseResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Mapping tests of the web layer: DTO to application command and domain to DTO.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md. Every assertion checks that no domain
 * entity is exposed and that the values travel unchanged.</p>
 */
class WebMapperTest {

    @Test
    void productMapperConvertsCommandAndResponse() {
        CreateProductRequest request = new CreateProductRequest(
                ProductType.PHYSICAL, ProductStatus.PUBLISHED, List.of("Color: negro"));

        RegisterProductCommand command = ProductWebMapper.toCommand(request);
        assertThat(command.type()).isEqualTo(ProductType.PHYSICAL);
        assertThat(command.status()).isEqualTo(ProductStatus.PUBLISHED);
        assertThat(command.variants()).containsExactly("Color: negro");

        Product product = new Product();
        product.setId(1L);
        product.setSellerId(2L);
        product.setType(ProductType.PHYSICAL);
        product.setVariants(List.of("Color: negro"));
        product.setStatus(ProductStatus.PUBLISHED);

        ProductResponse response = ProductWebMapper.toResponse(product);
        assertThat(response).isEqualTo(new ProductResponse(
                1L, 2L, ProductType.PHYSICAL, List.of("Color: negro"), ProductStatus.PUBLISHED));
        assertThat(ProductWebMapper.toResponses(List.of(product))).hasSize(1);
    }

    @Test
    void productMapperExposesTheStatusRequestValue() {
        assertThat(new ChangeProductStatusRequest(ProductStatus.SUSPENDED).status())
                .isEqualTo(ProductStatus.SUSPENDED);
    }

    @Test
    void inventoryMapperConvertsBothCommandsAndResponse() {
        RegisterStockCommand stock = InventoryWebMapper.toRegisterStockCommand(
                new RegisterStockRequest(1L, 2L, 7));
        assertThat(stock.productId()).isEqualTo(1L);
        assertThat(stock.warehouseId()).isEqualTo(2L);
        assertThat(stock.quantity()).isEqualTo(7);

        ReserveStockCommand reserve = InventoryWebMapper.toReserveStockCommand(
                new ReserveStockRequest(1L, 2L, 3));
        assertThat(reserve.amount()).isEqualTo(3);

        Inventory inventory = new Inventory();
        inventory.setId(9L);
        inventory.setProductId(1L);
        inventory.setWarehouseId(2L);
        inventory.setQuantity(4);
        inventory.setDamagedQuantity(1);

        InventoryResponse response = InventoryWebMapper.toResponse(inventory);
        assertThat(response).isEqualTo(new InventoryResponse(9L, 1L, 2L, 4, 1));
        assertThat(InventoryWebMapper.toResponses(List.of(inventory))).hasSize(1);
    }

    @Test
    void orderMapperConvertsTheResponse() {
        OrderResponse response = OrderWebMapper.toResponse(new Order(5L, 6L, OrderStatus.PAID));

        assertThat(response).isEqualTo(new OrderResponse(5L, 6L, OrderStatus.PAID));
    }

    @Test
    void refundMapperConvertsCommandAndResponse() {
        assertThat(RefundWebMapper.toCommand(new CreateRefundRequest(8L)).orderId()).isEqualTo(8L);

        Refund refund = new Refund();
        refund.setId(1L);
        refund.setOrderId(8L);

        assertThat(RefundWebMapper.toResponse(refund)).isEqualTo(new RefundResponse(1L, 8L));
    }

    @Test
    void warehouseMapperConvertsCommandAndResponse() {
        ManageWarehousesUseCase.RegisterCommand command = WarehouseWebMapper.toCommand(
                new CreateWarehouseRequest(WarehouseType.SELLER, 4L));
        assertThat(command.type()).isEqualTo(WarehouseType.SELLER);
        assertThat(command.sellerId()).isEqualTo(4L);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(3L);
        warehouse.setType(WarehouseType.SELLER);
        warehouse.setSellerId(4L);

        assertThat(WarehouseWebMapper.toResponse(warehouse))
                .isEqualTo(new WarehouseResponse(3L, WarehouseType.SELLER, 4L));
        assertThat(WarehouseWebMapper.toResponses(List.of(warehouse))).hasSize(1);
    }

    @Test
    void sellerMapperConvertsCommandAndResponse() {
        RegisterSellerUseCase.Command command = SellerWebMapper.toCommand(
                new CreateSellerRequest("Ada Lovelace", "ada@nexus.test", "DOC-1"));
        assertThat(command.fullName()).isEqualTo("Ada Lovelace");
        assertThat(command.email()).isEqualTo("ada@nexus.test");
        assertThat(command.identityDocument()).isEqualTo("DOC-1");

        Seller seller = new Seller();
        seller.setId(7L);
        seller.setUserId(70L);

        assertThat(SellerWebMapper.toResponse(seller)).isEqualTo(new SellerResponse(7L, 70L));
    }
}
