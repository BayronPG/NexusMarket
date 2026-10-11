package com.nexusmarket.application.port.in;

import com.nexusmarket.application.ActorContext;
import com.nexusmarket.domain.seller.Seller;

/**
 * Inbound port: register a seller and its underlying user.
 *
 * <p>EXPLICIT source: sellers do not self-register; they are incorporated by
 * the Administrator (RN-04, responsibilities matrix).</p>
 *
 * <p>Traceability: OBJ-02, RN-04, RN-14, RN-15, RG-03.</p>
 */
public interface RegisterSellerUseCase {

    /**
     * Input data of the use case.
     *
     * @param fullName         required, not empty (OBJ-01)
     * @param email            required, unique (RN-15)
     * @param identityDocument required, unique (RN-14)
     */
    record Command(String fullName, String email, String identityDocument) {
    }

    /**
     * Registers a seller, creating the underlying user with the SELLER role.
     *
     * @param actor   authenticated actor; must hold the ADMINISTRATOR role (RN-04)
     * @param command input data
     * @return the created seller
     */
    Seller registerSeller(ActorContext actor, Command command);

    /**
     * Finds a seller by its identifier.
     *
     * @param actor    authenticated actor holding the ADMINISTRATOR role
     * @param sellerId seller identifier
     * @return the seller
     */
    Seller findSeller(ActorContext actor, Long sellerId);
}
