package com.nexusmarket.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.application.port.in.ManageWarehousesUseCase.RegisterCommand;
import com.nexusmarket.application.port.in.RegisterSellerUseCase;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.warehouse.Warehouse;
import com.nexusmarket.domain.warehouse.WarehouseType;
import com.nexusmarket.infrastructure.web.dto.CreateSellerRequest;
import com.nexusmarket.infrastructure.web.dto.CreateWarehouseRequest;
import com.nexusmarket.infrastructure.web.dto.SellerResponse;
import com.nexusmarket.infrastructure.web.dto.WarehouseResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Behaviour of the seller and warehouse web mappers: null tolerance and
 * one-to-one field translation.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 */
class SellerAndWarehouseWebMapperTest {

    @Test
    void sellerCommandCarriesTheThreeRegistrationFields() {
        RegisterSellerUseCase.Command command = SellerWebMapper.toCommand(
                new CreateSellerRequest("Ana Torres", "ana@nexusmarket.com", "ID-001"));

        assertThat(command.fullName()).isEqualTo("Ana Torres");
        assertThat(command.email()).isEqualTo("ana@nexusmarket.com");
        assertThat(command.identityDocument()).isEqualTo("ID-001");
    }

    @Test
    void sellerMapperReturnsNullForNullInput() {
        assertThat(SellerWebMapper.toCommand(null)).isNull();
        assertThat(SellerWebMapper.toResponse(null)).isNull();
    }

    @Test
    void sellerResponseExposesIdAndUserId() {
        SellerResponse response = SellerWebMapper.toResponse(new Seller(7L, 9L));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.userId()).isEqualTo(9L);
    }

    @Test
    void warehouseCommandKeepsTypeAndSellerId() {
        RegisterCommand command = WarehouseWebMapper.toCommand(
                new CreateWarehouseRequest(WarehouseType.MARKETPLACE, null));

        assertThat(command.type()).isEqualTo(WarehouseType.MARKETPLACE);
        assertThat(command.sellerId()).isNull();
    }

    @Test
    void warehouseMapperReturnsNullForNullInput() {
        assertThat(WarehouseWebMapper.toCommand(null)).isNull();
        assertThat(WarehouseWebMapper.toResponse(null)).isNull();
    }

    @Test
    void warehouseResponsesMapEveryElementInOrder() {
        List<WarehouseResponse> responses = WarehouseWebMapper.toResponses(Arrays.asList(
                warehouse(1L, WarehouseType.SELLER, 4L),
                warehouse(2L, WarehouseType.MARKETPLACE, null)));

        assertThat(responses).extracting(WarehouseResponse::id).containsExactly(1L, 2L);
        assertThat(responses).extracting(WarehouseResponse::type)
                .containsExactly(WarehouseType.SELLER, WarehouseType.MARKETPLACE);
        assertThat(responses.get(1).sellerId()).isNull();
    }

    @Test
    void anEmptyWarehouseListMapsToAnEmptyList() {
        assertThat(WarehouseWebMapper.toResponses(List.of())).isEmpty();
    }

    private static Warehouse warehouse(Long id, WarehouseType type, Long sellerId) {
        Warehouse warehouse = new Warehouse(id, type);
        warehouse.setSellerId(sellerId);
        return warehouse;
    }
}
