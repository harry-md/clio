package com.harry.clio.repository;

import com.harry.clio.model.Book;
import com.harry.clio.model.BookStatus;
import com.harry.clio.model.BookType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookRepository
        extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @Query("""
        SELECT b
        FROM Book b
        JOIN FETCH b.categories c
        WHERE b.id = :bookId AND b.active = true AND b.type = :type AND b.status = :status
        """)
    Optional<Book> findWithCategoryById(
            @Param(value = "bookId") int bookId,
            @Param("type") BookType type,
            @Param("status") BookStatus status);

    @Query("""
        SELECT b
        FROM Book b
        WHERE b.active = true AND b.status = :status AND b.type = :type AND b.id IN :bookIds
        """)
    List<Book> findAllPurchasableByIdIn(
            List<Integer> bookIds,
            @Param("status") BookStatus status,
            @Param("type") BookType type);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Book b
        SET b.status = :status
        WHERE b.id = :bookId
        """)
    int updateStatus(@Param("bookId") int bookId, @Param("status") BookStatus status);

    @Query("""
        SELECT b
        FROM Book b
        WHERE b.active = true AND b.status = :status AND b.type = :type AND b.id = :bookId
        """)
    Optional<Book> findAvailableBookById(
            @Param("bookId") int bookId,
            @Param("status") BookStatus status,
            @Param("type") BookType type);

    @Modifying
    @Transactional
    @Query(value = """
        DELETE FROM book_categories
        WHERE book_id IN (:bookIds)
        """, nativeQuery = true)
    int deleteBookCategoryByBookIds(@Param("bookIds") List<Integer> bookIds);

    @Query("""
        SELECT b.id
        FROM Book b
        WHERE b.status = :status
        """)
    List<Integer> findIdsByStatus(@Param("status") BookStatus status);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Book b
        WHERE b.id IN :bookIds
        """)
    int deleteByBookIds(@Param("bookIds") List<Integer> bookIds);

    Optional<Book> findByIdAndType(int bookId, BookType type);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Book b
        SET b.encryptedFileUrl = :encryptedFileUrl,
            b.encryptedContentKey = :encryptedContentKey,
            b.status = :status,
            b.thumbnail = COALESCE(:thumbnail, b.thumbnail),
            b.updatedAt = CURRENT_TIMESTAMP
        WHERE b.id = :id
        """)
    int updateInfo(
            @Param("id") int id,
            @Param("encryptedFileUrl") String encryptedFileUrl,
            @Param("encryptedContentKey") String encryptedContentKey,
            @Param("thumbnail") String thumbnail,
            @Param("status") BookStatus status);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Book b
        SET b.rating =
            CASE
                WHEN b.ratingCount + :countDelta = 0 THEN 0
                ELSE
                    (COALESCE(b.rating, 0.0) * b.ratingCount + :ratingDelta) / (b.ratingCount + :countDelta)
            END,
            b.ratingCount = b.ratingCount + :countDelta
        WHERE b.id = :id AND b.status = :status AND b.type = :type
        """)
    int updateBookRating(
            @Param("id") int id,
            @Param("ratingDelta") int ratingDelta,
            @Param("countDelta") int countDelta,
            @Param("status") BookStatus status,
            @Param("type") BookType type);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE books b
        SET authors = (
            SELECT jsonb_agg(
               jsonb_build_object(
                    'authorId', a.id,
                    'authorFullname', a.full_name,
                    'role', ba.role
               )
            )
            FROM book_authors ba
            JOIN authors a ON ba.author_id = a.id
            WHERE b.id = ba.book_id
        )
        WHERE b.id IN (SELECT ba.book_id FROM book_authors ba WHERE ba.author_id = :authorId)
        """, nativeQuery = true)
    int updateAuthorsByAuthorId(@Param("authorId") int authorId);

    boolean existsByCategoriesId(int categoryId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Book b
        SET b.rating =
            CASE
                WHEN b.ratingCount + :countDelta = 0 THEN 0
                ELSE (COALESCE(b.rating, 0) * b.ratingCount + :ratingDelta) / (b.ratingCount + :countDelta)
            END,
            b.ratingCount = b.ratingCount + :countDelta,
            b.updatedAt = CURRENT_TIMESTAMP
        WHERE b.id = :bookId
            AND b.ratingCount > 0
            AND b.ratingCount + :countDelta >= 0
        """)
    int adjustExistingReviewRating(
            @Param("bookId") int bookId,
            @Param("ratingDelta") int ratingDelta,
            @Param("countDelta") int countDelta);
}
