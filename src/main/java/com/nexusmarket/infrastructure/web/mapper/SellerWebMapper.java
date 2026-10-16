package com.nexusmarket.infrastructure.web.mapper;

import com.nexusmarket.application.port.in.RegisterSellerUseCase;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.infrastructure.web.dto.CreateSellerRequest;
import com.nexusmarket.infrastructure.web.dto.SellerResponse;

/**
 * Explicit mapper between the seller web DTOs and the domain/application types.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 */
public final class SellerWebMapper {

    private SellerWebMapper() {
    }

    public static RegisterSellerUseCase.Command toCommand(CreateSellerRequest request) {
        if (request == null) {
            return null;
        }
        return new RegisterSellerUseCase.Command(
                request.fullName(), request.email(), request.identityDocument());
    }

    public static SellerResponse toResponse(Seller seller) {
        if (seller == null) {
            return null;
        }
        return new SellerResponse(seller.getId(), seller.getUserId());
    }
}
