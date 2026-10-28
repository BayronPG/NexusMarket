package com.nexusmarket.infrastructure.web.error;

import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates application and framework exceptions into HTTP responses.
 *
 * <p>Fase 2.4 of docs/PLAN_API_WEB.md. Before this class every exception
 * reached the container and was returned as a 500; now each known failure has
 * an explicit status and a uniform {@link ApiErrorResponse} body.</p>
 *
 * <p>Unexpected exceptions are logged with their stack trace but the response
 * only carries a generic message, so internal details are never leaked to the
 * client.</p>
 *
 * <p>Traceability: Fase 2.4, RG-01, RG-03, RN-14, RN-15.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String UNEXPECTED_ERROR_MESSAGE =
            "An unexpected error occurred. Please contact the administrator.";

    /**
     * A referenced element does not exist.
     *
     * @param exception the application exception
     * @param request   current request
     * @return 404 response
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            NotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    /**
     * The request body does not satisfy the Jakarta Validation constraints.
     *
     * @param exception the binding exception
     * @param request   current request
     * @return 400 response with the rejected fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidBody(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiErrorResponse.FieldValidationError> validationErrors =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(GlobalExceptionHandler::toFieldError)
                        .toList();
        return build(HttpStatus.BAD_REQUEST, "The request body is not valid", request, validationErrors);
    }

    /**
     * A validated method parameter (path variable or query parameter) is invalid.
     *
     * @param exception the constraint violation exception
     * @param request   current request
     * @return 400 response with the rejected parameters
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        List<ApiErrorResponse.FieldValidationError> validationErrors =
                exception.getConstraintViolations().stream()
                        .map(violation -> new ApiErrorResponse.FieldValidationError(
                                violation.getPropertyPath().toString(), violation.getMessage()))
                        .toList();
        return build(HttpStatus.BAD_REQUEST, "The request is not valid", request, validationErrors);
    }

    /**
     * The input of a use case is malformed, or a domain argument is illegal.
     *
     * @param exception the application or domain exception
     * @param request   current request
     * @return 400 response
     */
    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    /**
     * The actor is missing or its role is not allowed (RG-01, RG-03).
     *
     * @param exception the authorization exception
     * @param request   current request
     * @return 403 response
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            AuthorizationException exception, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    /**
     * A business rule that depends on existing state is violated, or an entity
     * is modified in an invalid state (for example RN-13).
     *
     * @param exception the business or domain exception
     * @param request   current request
     * @return 409 response
     */
    @ExceptionHandler({BusinessRuleException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(
            RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    /**
     * The JSON body is malformed or a path/query parameter has an invalid type.
     *
     * @param exception the framework exception
     * @param request   current request
     * @return 400 response
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "The request body or a parameter has an invalid format", request);
    }

    /**
     * A static resource or route that does not exist.
     *
     * @param exception the framework exception
     * @param request   current request
     * @return 404 response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingResource(
            NoResourceFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Resource not found: " + exception.getResourcePath(), request);
    }

    /**
     * Any other exception. The stack trace is logged, never returned.
     *
     * @param exception the unexpected exception
     * @param request   current request
     * @return 500 response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled exception while processing {} {}",
                request.getMethod(), request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_MESSAGE, request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {
        return build(status, message, request, null);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<ApiErrorResponse.FieldValidationError> validationErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors);
        return ResponseEntity.status(status).body(body);
    }

    private static ApiErrorResponse.FieldValidationError toFieldError(FieldError fieldError) {
        return new ApiErrorResponse.FieldValidationError(
                fieldError.getField(), fieldError.getDefaultMessage());
    }
}
