package com.nexusmarket.infrastructure.security;

import com.nexusmarket.application.port.out.TokenIssuer;
import com.nexusmarket.domain.user.Role;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * HS256 token issuer backed by the same encoder and secret used by the
 * resource server.
 *
 * <p>Claims: {@code sub} (positive numeric user id as String), {@code role}
 * (exact enum name), {@code iat} and {@code exp}. No personal data is added.</p>
 */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtEncoder encoder;
    private final long expirationSeconds;

    public JwtTokenIssuer(JwtEncoder encoder,
                          @Value("${jwt.expiration-seconds:900}") long expirationSeconds) {
        this.encoder = encoder;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public IssuedToken issue(Long userId, Role role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(now)
                .expiresAt(now.plus(expirationSeconds, ChronoUnit.SECONDS))
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new IssuedToken(token, expirationSeconds);
    }
}
