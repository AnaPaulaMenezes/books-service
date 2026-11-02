package com.studies.books_service.dto.idempotency;

public record IdempotentResponseDTO(
        int httpStatus,
        String responseBody) {
}
