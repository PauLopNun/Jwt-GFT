package com.exampleinyection.jwtgft.exception;

import com.exampleinyection.jwtgft.auth.TokenRefreshException;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(field -> formatFieldError(field))
            .collect(Collectors.joining(", "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problem.setTitle("Validation failed");
        problem.setType(URI.create("https://api.bookstore.dev/errors/validation"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ProblemDetail handleTokenRefresh(TokenRefreshException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Token refresh failed");
        problem.setType(URI.create("https://api.bookstore.dev/errors/token-refresh"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, BadCredentialsException.class})
    public ProblemDetail handleBusiness(RuntimeException ex) {
        HttpStatus status = ex instanceof BadCredentialsException ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle(status == HttpStatus.UNAUTHORIZED ? "Authentication required" : "Request rejected");
        problem.setType(URI.create("https://api.bookstore.dev/errors/business"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDenied(AuthorizationDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Insufficient permissions.");
        problem.setTitle("Access denied");
        problem.setType(URI.create("https://api.bookstore.dev/errors/access-denied"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred."
        );
        problem.setTitle("Internal server error");
        problem.setType(URI.create("https://api.bookstore.dev/errors/internal"));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}

