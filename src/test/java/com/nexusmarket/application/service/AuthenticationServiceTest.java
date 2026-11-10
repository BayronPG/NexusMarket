package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.error.InvalidCredentialsException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.AuthenticateUserUseCase.LoginCommand;
import com.nexusmarket.application.port.in.AuthenticateUserUseCase.LoginResult;
import com.nexusmarket.application.port.out.PasswordHasher;
import com.nexusmarket.application.port.out.TokenIssuer;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the login use case: normalization, generic errors and token issuing.
 */
class AuthenticationServiceTest {

    private static final String EMAIL = "buyer@nexusmarket.local";
    private static final String PASSWORD = "password";
    private static final String HASH = "stored-hash";

    private InMemoryUserRepository users;
    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        PasswordHasher hasher = (raw, encoded) ->
                PASSWORD.equals(raw) && HASH.equals(encoded);
        TokenIssuer issuer = (userId, role) ->
                new TokenIssuer.IssuedToken("token-" + userId + "-" + role, 900);
        service = new AuthenticationService(users, hasher, issuer);
    }

    @Test
    void loginReturnsATokenForValidCredentials() {
        long userId = saveUser(EMAIL, HASH, UserStatus.ACTIVE, Role.BUYER);

        LoginResult result = service.login(new LoginCommand(EMAIL, PASSWORD));

        assertThat(result.accessToken()).isEqualTo("token-" + userId + "-BUYER");
        assertThat(result.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void theEmailIsNormalizedWithTrimAndLowercase() {
        saveUser(EMAIL, HASH, UserStatus.ACTIVE, Role.BUYER);

        LoginResult result = service.login(new LoginCommand("  BUYER@NexusMarket.Local  ", PASSWORD));

        assertThat(result.accessToken()).contains("-BUYER");
    }

    @Test
    void aWrongPasswordProducesTheGenericError() {
        saveUser(EMAIL, HASH, UserStatus.ACTIVE, Role.BUYER);

        assertThatThrownBy(() -> service.login(new LoginCommand(EMAIL, "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void anUnknownEmailProducesTheSameGenericError() {
        saveUser(EMAIL, HASH, UserStatus.ACTIVE, Role.BUYER);

        assertThatThrownBy(() -> service.login(new LoginCommand("other@nexus.test", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void aDisabledUserCannotLogin() {
        saveUser(EMAIL, HASH, UserStatus.BLOCKED, Role.BUYER);

        assertThatThrownBy(() -> service.login(new LoginCommand(EMAIL, PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void aUserWithoutPasswordCannotLogin() {
        saveUser(EMAIL, null, UserStatus.ACTIVE, Role.BUYER);

        assertThatThrownBy(() -> service.login(new LoginCommand(EMAIL, PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void blankFieldsAreRejectedAsInvalidInput() {
        assertThatThrownBy(() -> service.login(new LoginCommand(" ", PASSWORD)))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.login(new LoginCommand(EMAIL, " ")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.login(null))
                .isInstanceOf(ValidationException.class);
    }

    private long saveUser(String email, String hash, UserStatus status, Role role) {
        User user = new User();
        user.setFullName("Usuario de prueba");
        user.setEmail(email);
        user.setIdentityDocument("DOC-" + email);
        user.setRole(role);
        user.setStatus(status);
        user.setPasswordHash(hash);
        return users.save(user).getId();
    }
}
