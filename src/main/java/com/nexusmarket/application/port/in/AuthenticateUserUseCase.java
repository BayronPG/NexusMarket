package com.nexusmarket.application.port.in;

/**
 * Inbound port: authentication of an existing user.
 *
 * <p>Traceability: authentication increment (login).</p>
 */
public interface AuthenticateUserUseCase {

    /**
     * Login input.
     *
     * @param email    raw email; it is normalized (trim + lowercase)
     * @param password raw password
     */
    record LoginCommand(String email, String password) {
    }

    /**
     * Login output; never contains the password or its hash.
     *
     * @param accessToken      signed JWT
     * @param expiresInSeconds validity in seconds
     */
    record LoginResult(String accessToken, long expiresInSeconds) {
    }

    /**
     * Authenticates the user and issues a token.
     *
     * @param command login input
     * @return the issued token
     * @throws com.nexusmarket.application.error.InvalidCredentialsException
     *         when the email does not exist, the password does not match or the
     *         user is not active
     */
    LoginResult login(LoginCommand command);
}
