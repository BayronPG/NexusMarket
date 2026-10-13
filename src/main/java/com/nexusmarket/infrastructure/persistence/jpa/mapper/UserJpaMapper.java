package com.nexusmarket.infrastructure.persistence.jpa.mapper;

import com.nexusmarket.domain.user.User;
import com.nexusmarket.infrastructure.persistence.jpa.entity.UserJpaEntity;

/**
 * Explicit mapper between the User domain aggregate and its JPA entity.
 *
 * <p>No mapping framework is used (Fase 1.2 constraint). Traceability: OBJ-01.</p>
 */
public final class UserJpaMapper {

    private UserJpaMapper() {
    }

    public static UserJpaEntity toJpa(User user) {
        if (user == null) {
            return null;
        }
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail());
        entity.setIdentityDocument(user.getIdentityDocument());
        entity.setRole(user.getRole());
        entity.setStatus(user.getStatus());
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        User user = new User();
        user.setId(entity.getId());
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setIdentityDocument(entity.getIdentityDocument());
        user.setRole(entity.getRole());
        user.setStatus(entity.getStatus());
        return user;
    }
}
