package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.seller.Seller;
import java.util.Optional;

/**
 * Output port for seller persistence.
 *
 * <p>Traceability: OBJ-02.</p>
 */
public interface SellerRepository {

    /** Stores the seller, assigning an identifier when it is new. */
    Seller save(Seller seller);

    /** Finds a seller by its identifier. */
    Optional<Seller> findById(Long id);

    /** Finds the seller associated with a user; each user holds a single role (RG-02). */
    Optional<Seller> findByUserId(Long userId);
}
