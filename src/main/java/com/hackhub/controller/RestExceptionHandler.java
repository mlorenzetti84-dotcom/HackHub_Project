package com.hackhub.controller;

import com.hackhub.domain.exception.DomainException;
import com.hackhub.domain.exception.InvalidStateTransitionException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException exception) {
        return buildError(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(InvalidStateTransitionException exception) {
        return buildError(HttpStatus.CONFLICT, "INVALID_HACKATHON_STATE", exception.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException exception) {
        return buildError(HttpStatus.CONFLICT, "DOMAIN_RULE_VIOLATION", exception.getMessage());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
        return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildError(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        code,
                        message == null || message.isBlank() ? status.getReasonPhrase() : message,
                        LocalDateTime.now()
                ));
    }

    public record ApiErrorResponse(int status, String code, String message, LocalDateTime timestamp) {
    }
}
