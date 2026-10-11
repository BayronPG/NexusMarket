package com.nexusmarket.application;

import com.nexusmarket.application.error.ValidationException;

/**
 * Small input checks shared by the application services.
 *
 * <p>Only malformed input is validated here. Business rules that depend on
 * existing state are verified against the output ports and domain entities.</p>
 */
public final class Validation {

    private Validation() {
    }

    /**
     * Requires a not null and not blank text.
     *
     * @param value text to check
     * @param field field name used in the error message
     * @return the same value when it is valid
     * @throws ValidationException if the value is null or blank
     */
    public static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required and cannot be blank");
        }
        return value;
    }

    /**
     * Requires a not null value.
     *
     * @param value value to check
     * @param field field name used in the error message
     * @param <T>   type of the value
     * @return the same value when it is valid
     * @throws ValidationException if the value is null
     */
    public static <T> T requireNotNull(T value, String field) {
        if (value == null) {
            throw new ValidationException(field + " is required");
        }
        return value;
    }
}
