package com.studies.books_service.service;

import com.studies.books_service.domain.book.Book;
import com.studies.books_service.domain.book.ReadingStatus;
import com.studies.books_service.dto.book.CreateBookDTO;
import com.studies.books_service.dto.book.UpdateBookDTO;
import com.studies.books_service.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    public Book createBook(CreateBookDTO book) {
        Book newBook = new Book();
        newBook.setCreatedAt(new Date());
        newBook.setTitle(book.title());
        newBook.setAuthor(book.author());
        newBook.setStatus(ReadingStatus.NOT_STARTED);

        UUID bookId = bookRepository.save(newBook).getId();
        newBook.setId(bookId);
        return newBook;
    }

    public Book updateBook(UUID id, UpdateBookDTO book) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        existingBook.setTitle(book.title());
        existingBook.setAuthor(book.author());
        existingBook.setStatus(book.status());
        existingBook.setRating(book.rating());

        if(book.startDate() == null && book.status() != ReadingStatus.NOT_STARTED) {
            existingBook.setStartDate(new Date());
        } else {
            existingBook.setStartDate(book.startDate());
        }

        if( book.finishDate() == null && book.status() == ReadingStatus.FINISHED) {
            existingBook.setFinishDate(new Date());
        } else {
            existingBook.setFinishDate(book.finishDate());
        }

        return bookRepository.save(existingBook);
    }

    public void deleteBook(UUID bookId) {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookRepository.delete(existingBook);
    }

    public Book getBookById(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public List<Book> getBookByStatus(ReadingStatus status, int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return bookRepository.findByStatus(status, pageable).getContent();
    }

}
