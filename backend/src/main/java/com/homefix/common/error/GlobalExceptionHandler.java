package com.homefix.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converts every failure into an RFC 9457 problem detail with two extension members:
 * {@code code} (an {@link ErrorCode}) and, for validation failures, {@code errors}.
 *
 * <p>Security filters delegate here too (see {@code ProblemDetailSecurityHandlers}), so
 * authentication and authorization failures share the same format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(ex.code(), ex.getMessage(), ex.violations(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(ErrorCode.UNAUTHORIZED, "Authentication is required", List.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(ErrorCode.ACCESS_DENIED, "You do not have permission to perform this action", List.of(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(v -> new FieldViolation(
                        v.getPropertyPath().toString(),
                        v.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                .getSimpleName(),
                        v.getMessage()))
                .toList();
        return build(ErrorCode.VALIDATION_FAILED, "Request validation failed", violations, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return build(
                ErrorCode.CONFLICT, "The resource was modified concurrently; reload and retry", List.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn(
                "Data integrity violation on {}: {}",
                request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.CONFLICT, "The request conflicts with existing data", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", List.of(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldViolation(e.getField(), e.getCode(), e.getDefaultMessage()))
                .toList();
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        return ResponseEntity.badRequest()
                .body(problem(ErrorCode.VALIDATION_FAILED, "Request validation failed", violations, servletRequest));
    }

    /** Adds a {@code code} to the problem details produced for standard Spring MVC exceptions. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail detail) {
            if (detail.getProperties() == null || !detail.getProperties().containsKey("code")) {
                detail.setProperty("code", codeFor(statusCode));
            }
            if (request instanceof ServletWebRequest servletRequest) {
                detail.setInstance(URI.create(servletRequest.getRequest().getRequestURI()));
            }
        }
        return response;
    }

    private static String codeFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> ErrorCode.MALFORMED_REQUEST.name();
            case 404 -> ErrorCode.NOT_FOUND.name();
            default -> status.is5xxServerError() ? ErrorCode.INTERNAL_ERROR.name() : "HTTP_" + status.value();
        };
    }

    private static ResponseEntity<ProblemDetail> build(
            ErrorCode code, String message, List<FieldViolation> violations, HttpServletRequest request) {
        return ResponseEntity.status(code.status()).body(problem(code, message, violations, request));
    }

    private static ProblemDetail problem(
            ErrorCode code, String message, List<FieldViolation> violations, HttpServletRequest request) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(code.status(), message);
        detail.setTitle(code.status().getReasonPhrase());
        detail.setInstance(URI.create(request.getRequestURI()));
        detail.setProperty("code", code.name());
        if (!violations.isEmpty()) {
            detail.setProperty("errors", violations);
        }
        return detail;
    }
}
