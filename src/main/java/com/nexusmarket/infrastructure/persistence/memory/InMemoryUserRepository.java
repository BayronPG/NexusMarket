package com.nexusmarket.infrastructure.persistence.memory;

import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.domain.user.User;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/**
 * TEMPORARY in-memory adapter for {@link UserRepository}.
 *
 * <p>It exists only to run and test the application services without choosing
 * a database. The persistence decision is PENDING (D-004); a Spring Data
 * adapter belongs to the API increment and will replace this class.</p>
 */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(sequence.incrementAndGet());
        }
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return store.values().stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByIdentityDocument(String identityDocument) {
        return store.values().stream()
                .filter(user -> user.getIdentityDocument() != null
                        && user.getIdentityDocument().equals(identityDocument))
                .findFirst();
    }
}
