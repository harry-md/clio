package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "subscription_book_billings",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_sub_book_billings_user_book",
                    columnNames = {"user_id", "book_id"})
        })
public class SubscriptionBookBilling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long pageCount = 0L;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
}
