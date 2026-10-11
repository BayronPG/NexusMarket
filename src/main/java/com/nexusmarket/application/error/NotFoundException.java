package com.nexusmarket.application.error;

/**
 * Raised when a use case references an element that does not exist.
 *
 * <p>Traceability: OBJ-01 to OBJ-11 (referenced records).</p>
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
