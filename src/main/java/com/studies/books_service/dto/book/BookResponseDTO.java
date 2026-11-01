package com.studies.books_service.dto.book;

import com.studies.books_service.domain.book.Book;
import com.studies.books_service.domain.book.ReadingStatus;
import java.util.Date;
import java.util.UUID;

public record BookResponseDTO(
        UUID id,
        String title,
        String author,
        ReadingStatus status,
        Integer rating,
        Date startDate,
        Date finishDate,
        String coverUrl,
        Date createdAt
) {
    public static BookResponseDTO from(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getStatus(),
                book.getRating(),
                book.getStartDate(),
                book.getFinishDate(),
                book.getCoverUrl(),
                book.getCreatedAt());
    }
}