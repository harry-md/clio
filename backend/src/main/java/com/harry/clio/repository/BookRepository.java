package com.harry.clio.repository;

import com.harry.clio.entity.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository
        extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @Query("""
        SELECT b
        FROM Book b
        JOIN FETCH b.bookInfo bi
        JOIN FETCH b.categories c
        WHERE b.id = :bookId
        """)
    Optional<Book> findWithDetailById(@Param(value = "bookId") Integer bookId);
}
