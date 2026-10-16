package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.nexusmarket.infrastructure.persistence.jpa.mapper.UserJpaMapper;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataUserRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * JPA adapter for the {@link UserRepository} output port.
 *
 * <p>It converts the domain with {@link UserJpaMapper} and delegates the persistence
 * to {@link SpringDataUserRepository}. It implements only the methods declared by the
 * port and contains no business rules (those stay in the domain and the application
 * services).</p>
 *
 * <p>Fase 1.3.2 of docs/PLAN_API_WEB.md.</p>
 */
@Repository
@Profile("!memory")
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public JpaUserRepositoryAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = springDataUserRepository.save(UserJpaMapper.toJpa(user));
        return UserJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByIdentityDocument(String identityDocument) {
        return springDataUserRepository.findByIdentityDocument(identityDocument)
                .map(UserJpaMapper::toDomain);
    }
}
