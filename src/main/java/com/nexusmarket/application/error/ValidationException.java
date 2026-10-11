package com.nexusmarket.application.error;

/**
 * Raised when the input of a use case does not satisfy a required condition
 * (for example a blank full name or a missing required attribute).
 *
 * <p>Traceability: OBJ-01 (required attributes of a user).</p>
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
