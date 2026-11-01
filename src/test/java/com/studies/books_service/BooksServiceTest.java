package com.studies.books_service;

import com.studies.books_service.domain.book.Book;
import com.studies.books_service.domain.book.ReadingStatus;
import com.studies.books_service.dto.book.UpdateBookDTO;
import com.studies.books_service.dto.event.BookFinishedEvent;
import com.studies.books_service.repository.BookRepository;
import com.studies.books_service.service.BookEventPublisher;
import com.studies.books_service.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BooksServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookEventPublisher eventPublisher;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldPublishEventWhenStatusChangesToRead() {
        UUID id = UUID.randomUUID();
        Book book = bookWithStatus(id, ReadingStatus.READING);          // ajuste
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(id, readRequest(5, new Date(2026, 10, 12)));   // ajuste

        ArgumentCaptor<BookFinishedEvent> captor = ArgumentCaptor.forClass(BookFinishedEvent.class);
        verify(eventPublisher).publishBookFinishedEvent(captor.capture());
        assertThat(captor.getValue().bookId()).isEqualTo(id);
        assertThat(captor.getValue().rating()).isEqualTo(5);
    }

    @Test
    void shouldNotPublishEventWhenBookWasAlreadyRead() {
        UUID id = UUID.randomUUID();
        Book book = bookWithStatus(id, ReadingStatus.FINISHED);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateBook(id, readRequest(4, new Date(2026, 10, 12)));

        verify(eventPublisher, never()).publishBookFinishedEvent(any());
    }

    @Test
    void shouldThrowAndNotSaveWhenBookDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(id, readRequest(5, new Date())))
                .isInstanceOf(RuntimeException.class).message().isEqualTo("Book not found");// ajuste

        verify(bookRepository, never()).save(any());
        verify(eventPublisher, never()).publishBookFinishedEvent(any());
    }

    // helpers: ajuste aos construtores/setters reais de Book e BookRequestDTO
    private Book bookWithStatus(UUID id, ReadingStatus status) {
        Book newbook = new Book();
        newbook.setId(id);
        newbook.setStatus(status);
        return newbook;
    }
    private UpdateBookDTO readRequest(Integer rating, Date finishDate) {
        UpdateBookDTO newbook = new UpdateBookDTO(
                "Teste",
                "Teste",
                ReadingStatus.FINISHED,
                rating,
                new Date(),
                finishDate
        );
        return newbook;
    }
}
