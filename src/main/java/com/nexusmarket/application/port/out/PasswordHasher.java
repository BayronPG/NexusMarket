package com.nexusmarket.application.port.out;

/**
 * Output port that verifies a raw password against a stored hash.
 *
 * <p>Traceability: authentication increment (login).</p>
 */
public interface PasswordHasher {

    /**
     * Verifies a raw password against a BCrypt hash.
     *
     * @param rawPassword     password sent by the user
     * @param encodedPassword stored hash; may be null for users without password
     * @return {@code true} only when the password matches
     */
    boolean matches(String rawPassword, String encodedPassword);
}
