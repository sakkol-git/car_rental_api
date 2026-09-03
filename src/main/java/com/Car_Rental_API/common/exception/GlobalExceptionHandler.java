package com.Car_Rental_API.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.Car_Rental_API.common.util.TelegramUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global Exception Handler providing RFC 7807 Problem Details.
 * The "errors" extension property is reserved for genuinely multi-field
 * validation failures; every other exception relies on "detail" alone,
 * which RFC 7807 already provides for a single human-readable cause.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    private static final String PROPERTY_TIMESTAMP = "timestamp";
    private static final String PROPERTY_ERRORS = "errors";
    private static final Map<String, String> NO_FIELD_ERRORS = Collections.emptyMap();

    private final TelegramUtil telegramUtil;

    @Value("${spring.profiles.active:dev}")
    private String appEnv;

    // =========================================================================
    // 1. Validation & Binding Exceptions (400 Bad Request) — genuine multi-field errors
    // =========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.merge(fieldError.getField(),
                    Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value"),
                    (existing, msg) -> existing + "; " + msg);
        }
        for (ObjectError globalError : ex.getBindingResult().getGlobalErrors()) {
            errors.merge(globalError.getObjectName(),
                    Optional.ofNullable(globalError.getDefaultMessage()).orElse("Validation failed"),
                    (existing, msg) -> existing + "; " + msg);
        }
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Request validation failed for one or more fields", errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyName = StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                    .reduce((first, second) -> second)
                    .map(Path.Node::getName)
                    .orElse(violation.getPropertyPath().toString());
            errors.merge(propertyName,
                    Optional.ofNullable(violation.getMessage()).orElse("Invalid constraint"),
                    (existing, msg) -> existing + "; " + msg);
        }
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Constraint Violation",
                "One or more parameters violated validation constraints", errors, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();
            String message = result.getResolvableErrors().stream()
                    .map(error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid parameter"))
                    .collect(Collectors.joining("; "));
            errors.put(paramName != null ? paramName : "parameter", message);
        });
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Parameter Validation Failed",
                "Method parameter validation failed", errors, request);
    }

    // =========================================================================
    // 2. HTTP Request & Parsing Exceptions — single-cause, "detail" is sufficient
    // =========================================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed HTTP message body: {}", ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Malformed Request Body",
                "Required request body is missing or contains malformed JSON", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Type Mismatch",
                "Value '%s' is not valid for parameter '%s' (expected type: %s)"
                        .formatted(ex.getValue(), ex.getName(), requiredType),
                request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Missing Required Parameter",
                "Required parameter '%s' of type '%s' is missing"
                        .formatted(ex.getParameterName(), ex.getParameterType()),
                request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found",
                "Resource not found: " + request.getRequestURI(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String detail = "Request method '%s' is not supported for this endpoint".formatted(ex.getMethod());
        if (ex.getSupportedHttpMethods() != null) {
            detail += ". Supported methods: " + ex.getSupportedHttpMethods();
        }
        return buildProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", detail, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type",
                "Content-Type '%s' is not supported. Supported types: %s"
                        .formatted(ex.getContentType(), ex.getSupportedMediaTypes()),
                request);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.NOT_ACCEPTABLE, "Media Type Not Acceptable",
                Optional.ofNullable(ex.getMessage())
                        .orElse("Requested media type cannot be produced according to Accept headers"),
                request);
    }

    // =========================================================================
    // 3. Security Exceptions (401, 403)
    // =========================================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.FORBIDDEN, "Access Denied",
                "You do not have permission to access this resource", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized",
                Optional.ofNullable(ex.getMessage()).orElse("Authentication is required to access this resource"),
                request);
    }

    // =========================================================================
    // 4. Business & Domain Logic Exceptions (400, 409, Custom)
    // =========================================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Argument",
                Optional.ofNullable(ex.getMessage()).orElse("Invalid argument provided"), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Resource Conflict",
                Optional.ofNullable(ex.getMessage()).orElse("Conflict with current resource state"), request);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(
            GlobalException ex, HttpServletRequest request) {

        HttpStatus status = Optional.ofNullable(HttpStatus.resolve(ex.getStatus()))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        String rawMessage = ex.getMessage();
        boolean looksTechnical = rawMessage == null || rawMessage.contains("Exception") || rawMessage.contains("java.");
        String detail = (status.is5xxServerError() && looksTechnical)
                ? "An unexpected server error occurred"
                : Optional.ofNullable(rawMessage).orElse("An application error occurred");

        return buildProblemDetail(status, status.getReasonPhrase(), detail, request);
    }

    // =========================================================================
    // 5. Catch-All Unhandled Exceptions (500 Internal Server Error)
    // =========================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnhandledException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception processing request [{} {}]", request.getMethod(), request.getRequestURI(), ex);
        sendTelegramAlert(ex, request);

        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected internal server error occurred. Please try again later.",
                request);
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /** Single-cause response: "errors" is present but empty for a consistent response shape. */
    private ResponseEntity<ProblemDetail> buildProblemDetail(
            HttpStatus status, String title, String detail, HttpServletRequest request) {
        return buildProblemDetail(status, title, detail, NO_FIELD_ERRORS, request);
    }

    /** Multi-field response, used only by the three validation handlers above. */
    private ResponseEntity<ProblemDetail> buildProblemDetail(
            HttpStatus status, String title, String detail, Map<String, String> errors, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        problemDetail.setProperty(PROPERTY_ERRORS, errors);

        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .body(problemDetail);
    }

    private String extractUsername(HttpServletRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return Optional.ofNullable(req.getHeader("X-User-Username")).orElse("Anonymous");
    }

    private String extractErrorMessage(Throwable ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause != rootCause.getCause()) {
            rootCause = rootCause.getCause();
        }
        String msg = Optional.ofNullable(rootCause.getMessage()).orElse("Unknown Error");
        return msg.length() > 300 ? msg.substring(0, 300) + "..." : msg;
    }

    private String extractStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        return trace.length() > 3000 ? trace.substring(0, 3000) + "..." : trace;
    }

    private void sendTelegramAlert(Exception ex, HttpServletRequest request) {
        try {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    resolveStatus(ex),
                    extractErrorMessage(ex));

            problemDetail.setProperty("endpoint", request.getRequestURI());
            problemDetail.setProperty("method", request.getMethod());
            problemDetail.setProperty("username", extractUsername(request));
            problemDetail.setProperty("mode", appEnv);
            problemDetail.setProperty("stackTrace", extractStackTrace(ex));

            telegramUtil.sendErrorNotification(problemDetail);
        } catch (Exception e) {
            log.warn("Telegram alert notification failed: {}", e.getMessage(), e);
        }
    }

    private HttpStatus resolveStatus(Exception ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}