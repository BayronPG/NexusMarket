package com.nexusmarket.application.error;

/**
 * Credentials are not valid. The message is always generic so the caller
 * cannot tell whether the email exists.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
