package com.studies.books_service.controller;

import com.studies.books_service.domain.book.Book;
import com.studies.books_service.domain.book.ReadingStatus;
import com.studies.books_service.dto.idempotency.IdempotentResponseDTO;
import com.studies.books_service.dto.book.BookResponseDTO;
import com.studies.books_service.dto.book.CreateBookDTO;
import com.studies.books_service.dto.book.UpdateBookDTO;
import com.studies.books_service.service.BookService;
import com.studies.books_service.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private JsonMapper jsonMapper;

    @PostMapping
    public ResponseEntity<String> createBook(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody CreateBookDTO request
    ) {
       IdempotentResponseDTO response = idempotencyService.execute(key, request, () -> {
           Book createdBook = bookService.createBook(request);
           BookResponseDTO dto = BookResponseDTO.from(createdBook);
           return new IdempotentResponseDTO(HttpStatus.CREATED.value(), jsonMapper.writeValueAsString(dto));
        });

        return ResponseEntity.status(response.httpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.responseBody());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable UUID id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/status")
    public ResponseEntity<java.util.List<Book>> getBooksByStatus(
            @RequestParam ReadingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        java.util.List<Book> books = bookService.getBookByStatus(status, page, size);
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable UUID id, @RequestBody UpdateBookDTO book) {
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
