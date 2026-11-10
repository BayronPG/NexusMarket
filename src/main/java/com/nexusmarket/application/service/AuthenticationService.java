package com.nexusmarket.application.service;

import com.nexusmarket.application.error.InvalidCredentialsException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.AuthenticateUserUseCase;
import com.nexusmarket.application.port.out.PasswordHasher;
import com.nexusmarket.application.port.out.TokenIssuer;
import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Application service that authenticates an existing user.
 *
 * <p>The email is normalized (trim + lowercase) and looked up
 * case-insensitively. Every failure (unknown email, wrong password, disabled
 * user, user without password) produces the same generic error, so the caller
 * cannot discover whether an account exists.</p>
 *
 * <p>Traceability: authentication increment (login), RG-01.</p>
 */
@Service
public class AuthenticationService implements AuthenticateUserUseCase {

    private static final int MIN_PASSWORD_LENGTH = 1;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordHasher passwordHasher,
                                 TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        if (command == null) {
            throw new ValidationException("email and password are required");
        }
        String email = normalize(command.email());
        String password = command.password();
        if (email.isEmpty() || password == null
                || password.trim().length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("email and password are required");
        }

        User user = userRepository.findByEmailNormalized(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() != UserStatus.ACTIVE
                || !passwordHasher.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenIssuer.IssuedToken token = tokenIssuer.issue(user.getId(), user.getRole());
        return new LoginResult(token.accessToken(), token.expiresInSeconds());
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
