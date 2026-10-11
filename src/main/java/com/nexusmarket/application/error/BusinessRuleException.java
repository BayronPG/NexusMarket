package com.nexusmarket.application.error;

/**
 * Raised when a business rule must be verified against state that lives
 * outside a single entity (for example uniqueness of an email or of an
 * identity document, RN-14 and RN-15).
 *
 * <p>Rules that a single entity can enforce stay in the domain
 * ({@code Inventory.reserve}, {@code Order.changeStatus}) and are not
 * duplicated here.</p>
 *
 * <p>Traceability: RN-04, RN-14, RN-15.</p>
 */
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(message);
    }
}
