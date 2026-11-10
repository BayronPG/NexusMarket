package com.nexusmarket.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies the stateless JWT resource server.
 *
 * <p>The secret used here is a fake value declared in
 * {@code application-test.properties}; it is never a real credential.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    /** Fake secret shared with application-test.properties (test scope only). */
    private static final String FAKE_SECRET = "test-only-fake-secret-do-not-use-in-production-2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void swaggerIsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void apiRequiresToken() throws Exception {
        mockMvc.perform(get("/api/products/published"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validJwtAuthenticatesApi() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .header("Authorization", "Bearer " + token("25", "BUYER", Instant.now().plusSeconds(60))))
                .andExpect(status().isOk());
    }

    @Test
    void expiredJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .header("Authorization", "Bearer " + token("25", "BUYER", Instant.now().minusSeconds(60))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownRoleIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .header("Authorization", "Bearer " + token("25", "WIZARD", Instant.now().plusSeconds(60))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonNumericSubjectIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .header("Authorization", "Bearer " + token("buyer-25", "BUYER", Instant.now().plusSeconds(60))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void negativeSubjectIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .header("Authorization", "Bearer " + token("-5", "BUYER", Instant.now().plusSeconds(60))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/products/published")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleNotAllowedByTheUseCaseReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token("25", "BUYER", Instant.now().plusSeconds(60)))
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"PHYSICAL\",\"status\":\"PUBLISHED\",\"variants\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleBecomesRoleAuthority() {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("25")
                .claim("role", "SELLER")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();

        assertThat(jwtAuthenticationConverter.convert(jwt).getAuthorities())
                .containsExactly(new SimpleGrantedAuthority("ROLE_SELLER"));
    }

    @Test
    void shortSecretIsRejected() {
        SecurityConfig config = new SecurityConfig();

        assertThatThrownBy(() -> config.jwtDecoder("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    private static String token(String subject, String role, Instant expiresAt) {
        SecretKeySpec key = new SecretKeySpec(FAKE_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(key)
                .algorithm(com.nimbusds.jose.JWSAlgorithm.HS256)
                .build();
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(jwk));
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(source);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(expiresAt.minusSeconds(120))
                .expiresAt(expiresAt)
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims))
                .getTokenValue();
    }
}
