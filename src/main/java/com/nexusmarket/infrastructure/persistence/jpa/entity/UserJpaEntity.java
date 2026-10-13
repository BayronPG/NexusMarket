package com.nexusmarket.infrastructure.persistence.jpa.entity;

import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence entity for the User aggregate.
 *
 * <p>The domain class {@code com.nexusmarket.domain.user.User} stays free of
 * framework annotations; this class only mirrors its attributes for persistence
 * and is converted by {@code UserJpaMapper}.</p>
 *
 * <p>No JPA relationship is declared: the domain does not reference other
 * aggregates by object. Traceability: OBJ-01, RN-01, RN-02, RN-14, RN-15
 * (Fase 1.2 of docs/PLAN_API_WEB.md).</p>
 */
@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "identity_document")
    private String identityDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
