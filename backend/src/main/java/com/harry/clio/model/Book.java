package com.harry.clio.model;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault(
            "'https://res.cloudinary.com/dswxedhsf/image/upload/v1782883664/book_fgsg7m.jpg'")
    private String thumbnail =
            "https://res.cloudinary.com/dswxedhsf/image/upload/v1782883664/book_fgsg7m.jpg";

    @Builder.Default
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private BookType type = BookType.SYSTEM;

    @Column(nullable = true)
    private String fileUrl;

    @Column(nullable = true)
    private String encryptedFileUrl;

    @Column(nullable = true)
    private String encryptedContentKey;

    @Column(nullable = true)
    private Double rating;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    private Long ratingCount = 0L;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = true, columnDefinition = "JSONB")
    private List<BookAuthorResponse> authors;

    @Builder.Default
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BookStatus status = BookStatus.QUEUED;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "publisher_id", nullable = true)
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "uploader_id", nullable = true)
    private User uploader;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;
}
