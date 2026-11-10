package com.nexusmarket.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link BCryptPasswordHasher}: BCrypt with strength 12 and safe
 * handling of missing or malformed hashes.
 */
class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void hashesWithBcryptStrengthTwelveAndVerifiesThePassword() {
        String hash = hasher.hash("password");

        assertThat(hash).startsWith("$2a$12$");
        assertThat(hasher.matches("password", hash)).isTrue();
        assertThat(hasher.matches("wrong", hash)).isFalse();
    }

    @Test
    void missingOrMalformedHashesNeverMatch() {
        assertThat(hasher.matches("password", null)).isFalse();
        assertThat(hasher.matches("password", " ")).isFalse();
        assertThat(hasher.matches("password", "not-a-bcrypt-hash")).isFalse();
        assertThat(hasher.matches(null, hasher.hash("password"))).isFalse();
    }
}
