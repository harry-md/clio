package com.harry.clio.book;

import com.harry.clio.category.Category;
import com.harry.clio.user.Publisher;
import com.harry.clio.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "books",
        indexes = {
            @Index(name = "idx_books_created_at", columnList = "created_at DESC"),
            @Index(name = "idx_books_publisher_id", columnList = "publisher_id"),
            @Index(name = "idx_books_uploader_id", columnList = "uploader_id")
        })
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    @ColumnDefault(
            "'https://res.cloudinary.com/dswxedhsf/image/upload/v1782883664/book_fgsg7m.jpg'")
    private String thumbnail =
            "https://res.cloudinary.com/dswxedhsf/image/upload/v1782883664/book_fgsg7m.jpg";

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private BookType type = BookType.SYSTEM;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = true)
    private Double rating;

    @ColumnDefault("0")
    private Integer ratingCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = true, columnDefinition = "JSONB")
    private List<BookAuthorJson> authors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = true)
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = true)
    private User uploader;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    private Instant updatedAt;
}
