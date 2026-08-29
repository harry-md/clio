package com.harry.clio.repository;

import com.harry.clio.model.Book;
import com.harry.clio.model.BookStatus;
import com.harry.clio.model.BookType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
            @Param(value = "bookId") Integer bookId,
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

    @Transactional
    @Modifying
    @Query("""
        UPDATE Book b
        SET b.status = :status
        WHERE b.id = :bookId
        """)
    int updateStatus(@Param("bookId") Integer bookId, @Param("status") BookStatus status);

    @Query("""
        SELECT b
        FROM Book b
        WHERE b.active = true AND b.status = :status AND b.type = :type AND b.id = :bookId
        """)
    Optional<Book> findAddableBookById(
            @Param("bookId") Integer bookId,
            @Param("status") BookStatus status,
            @Param("type") BookType type);

    @Transactional
    @Modifying
    @Query("""
        DELETE FROM Book b
        WHERE b.status = :status
        """)
    int deleteFailedBooks(@Param("status") BookStatus status);

    Optional<Book> findByIdAndType(Integer bookId, BookType type);

    @Transactional
    @Modifying
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
}
