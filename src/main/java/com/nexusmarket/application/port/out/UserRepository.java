package com.nexusmarket.application.port.out;

import com.nexusmarket.domain.user.User;
import java.util.Optional;

/**
 * Output port for user persistence.
 *
 * <p>Traceability: OBJ-01, RN-14, RN-15.</p>
 */
public interface UserRepository {

    /** Stores the user, assigning an identifier when it is new. */
    User save(User user);

    /** Finds a user by its identifier. */
    Optional<User> findById(Long id);

    /** Finds a user by email; used to enforce uniqueness (RN-15). */
    Optional<User> findByEmail(String email);

    /** Finds a user by identity document; used to enforce uniqueness (RN-14). */
    Optional<User> findByIdentityDocument(String identityDocument);
}
