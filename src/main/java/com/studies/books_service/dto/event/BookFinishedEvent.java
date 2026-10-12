package com.studies.books_service.dto.event;

import java.util.Date;
import java.util.UUID;

public record BookFinishedEvent(
        UUID bookId,
        String title,
        String author,
        Integer rating,
        Date finishDate
) {
}
