package com.studies.books_service.domain.book;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "book")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Book {
    @Id
    @GeneratedValue
    private UUID id;
    private String title;
    private String author;

    @Enumerated(EnumType.STRING)
    private ReadingStatus status;

    private Integer rating;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "finish_date")
    private Date finishDate;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name="created_at")
    private Date createdAt;

}
