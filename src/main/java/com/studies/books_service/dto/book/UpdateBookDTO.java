package com.studies.books_service.dto.book;

import com.studies.books_service.domain.book.ReadingStatus;

import java.util.UUID;
import java.util.Date;

public record UpdateBookDTO(
        String title,
        String author,
        ReadingStatus status,
        Integer rating,
        Date startDate,
        Date finishDate
) {
}
