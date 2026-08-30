package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_reviews_user_book",
                    columnNames = {"user_id", "book_id"})
        })
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", length = 500)
    private String comment;
}
