package com.nexusmarket.infrastructure.security;

import com.nexusmarket.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt implementation of {@link PasswordHasher} with strength 12.
 *
 * <p>The raw password is never stored or logged; only the hash travels to
 * persistence.</p>
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int STRENGTH = 12;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(STRENGTH);

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        try {
            return encoder.matches(rawPassword, encodedPassword);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Hashes a raw password; used only to provision test/demo users.
     *
     * @param rawPassword raw password
     * @return BCrypt hash
     */
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
