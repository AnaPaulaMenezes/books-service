package com.studies.books_service.domain.idempotency;

public record IdempotentResponse(
        int httpStatus,
        String responseBody) {
}
