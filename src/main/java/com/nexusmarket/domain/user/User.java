package com.nexusmarket.domain.user;

/**
 * User of the NexusMarket platform.
 *
 * <p>EXPLICIT source: "personas autorizadas para interactuar con el sistema".
 * Attributes: identifier (unique, required), full name (required, not empty),
 * email (required, unique), role (required, unique per user) and status
 * (required).</p>
 *
 * <p>INTERPRETATION: {@code identityDocument} is derived from the critical
 * validation "documento de identidad ... único"; the attribute table of the
 * specification does not list it explicitly.</p>
 *
 * <p>Traceability: OBJ-01, RN-01, RN-02, RN-14, RN-15.</p>
 */
public class User {

    /** Unique identifier of the user (EXPLICIT: "Identificador ... Único"). */
    private Long id;

    /** Official full name of the user; required and not empty. */
    private String fullName;

    /** Main access and communication mean; required and unique. */
    private String email;

    /** Identity document; INTERPRETATION derived from the critical validation of uniqueness. */
    private String identityDocument;

    /** Defines responsibilities and permissions; unique per user (RG-02). */
    private Role role;

    /** Operational status of the user. */
    private UserStatus status;

    public User() {
    }

    public User(Long id, String fullName, String email, Role role, UserStatus status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }

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
