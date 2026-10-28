package com.nexusmarket.infrastructure.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Uniform body returned by every error of the REST boundary.
 *
 * <p>Fase 2.4 of docs/PLAN_API_WEB.md. The same shape is used for every HTTP
 * status so clients do not need to parse different error formats.</p>
 *
 * <p>{@code validationErrors} is present only for requests rejected by input
 * validation; for every other error the field is omitted from the JSON.</p>
 *
 * @param timestamp        moment in which the error was produced
 * @param status           HTTP status code
 * @param error            HTTP reason phrase
 * @param message          human readable description
 * @param path             request path that produced the error
 * @param validationErrors field errors, only when input validation failed
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> validationErrors) {

    public ApiErrorResponse {
        validationErrors = validationErrors == null ? null : List.copyOf(validationErrors);
    }

    /**
     * One rejected field of the request.
     *
     * @param field   name of the field
     * @param message validation message
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record FieldValidationError(String field, String message) {
    }
}
