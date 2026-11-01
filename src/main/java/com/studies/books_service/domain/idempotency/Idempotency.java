package com.studies.books_service.domain.idempotency;

public record Idempotency(
        IdempotencyStatus status,
        String requestHash,
        Integer httpStatus,
        String responseBody
) {}