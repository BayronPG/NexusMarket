package com.nexusmarket.infrastructure.persistence.jpa.repository;

import com.nexusmarket.infrastructure.persistence.jpa.entity.UserJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Technical Spring Data repository for {@link UserJpaEntity}.
 *
 * <p>It declares only the queries required by the
 * {@code com.nexusmarket.application.port.out.UserRepository} port. {@code save},
 * {@code findById} and {@code findAll} are inherited from {@link JpaRepository}.</p>
 *
 * <p>The derived query names use the attribute names of the JPA entity exactly.
 * Fase 1.3.1 of docs/PLAN_API_WEB.md.</p>
 */
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    /** Mirrors UserRepository.findByEmail (RN-15 uniqueness check). */
    Optional<UserJpaEntity> findByEmail(String email);

    /** Mirrors UserRepository.findByIdentityDocument (RN-14 uniqueness check). */
    Optional<UserJpaEntity> findByIdentityDocument(String identityDocument);
}
