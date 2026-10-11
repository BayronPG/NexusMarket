package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.buyer.Buyer;
import java.util.Optional;

/**
 * Output port for buyer persistence.
 *
 * <p>Buyer registration is not implemented in this increment because the
 * source does not define the participant that performs it; the port already
 * exists because orders reference a buyer.</p>
 *
 * <p>Traceability: OBJ-03, OBJ-08.</p>
 */
public interface BuyerRepository {

    /** Stores the buyer, assigning an identifier when it is new. */
    Buyer save(Buyer buyer);

    /** Finds a buyer by its identifier. */
    Optional<Buyer> findById(Long id);

    /** Finds the buyer associated with a user; each user holds a single role (RG-02). */
    Optional<Buyer> findByUserId(Long userId);
}
