package com.harry.clio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "book_authors",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_book_author_role",
                    columnNames = {"book_id", "author_id", "role"})
        })
public class BookAuthor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Builder.Default
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BookAuthorRole role = BookAuthorRole.AUTHOR;
}
