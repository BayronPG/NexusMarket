package com.nexusmarket.application.error;

/**
 * Raised when the actor is not authenticated (RG-01) or its role is not
 * allowed for the use case (RG-03).
 *
 * <p>Traceability: RG-01, RG-03.</p>
 */
public class AuthorizationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(message);
    }
}
