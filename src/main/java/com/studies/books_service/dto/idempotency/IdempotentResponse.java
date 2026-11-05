package com.studies.books_service.dto.idempotency;

public record IdempotentResponse(
        int httpStatus,
        String responseBody) {
}
