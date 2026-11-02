package com.studies.books_service.exceptions;

public class IdempotencyKeyReuseException extends RuntimeException {
    public IdempotencyKeyReuseException(String message) {
        super(message);
    }
}
