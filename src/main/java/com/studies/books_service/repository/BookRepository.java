package com.studies.books_service.repository;

import com.studies.books_service.domain.book.Book;

import com.studies.books_service.domain.book.ReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    @Query("SELECT b FROM Book b WHERE b.status = :status ORDER BY b.createdAt ASC")
    public Page<Book> findByStatus(@Param("status") ReadingStatus status, Pageable pageable);
}
