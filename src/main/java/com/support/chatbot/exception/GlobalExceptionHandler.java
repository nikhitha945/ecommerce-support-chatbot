package com.support.chatbot.exception;

import com.support.chatbot.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceAccessException.class)   // Ollama unreachable
    public ResponseEntity<ErrorResponse> handleAiDown(ResourceAccessException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "AI service is temporarily unavailable, please try again.", req);
    }

    @ExceptionHandler(DataAccessException.class)        // Postgres/pgvector unreachable
    public ResponseEntity<ErrorResponse> handleDbDown(DataAccessException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Knowledge base is temporarily unavailable, please try again.", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)  // bad input, e.g. blank question
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)                  // everything else: generic, never leak internals
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, please try again.", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }
}
