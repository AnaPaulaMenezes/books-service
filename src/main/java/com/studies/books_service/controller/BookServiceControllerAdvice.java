package com.studies.books_service.controller;

import com.studies.books_service.exceptions.IdempotencyConflictException;
import com.studies.books_service.exceptions.IdempotencyKeyReuseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BookServiceControllerAdvice {
    @ExceptionHandler(IdempotencyKeyReuseException.class)
    public ResponseEntity<String> handleIdempotencyKeyReuseException(IdempotencyKeyReuseException ex) {
        return ResponseEntity.status(422).body(ex.getMessage());
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<String> handleIdempotencyConflictException(IdempotencyConflictException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

}
