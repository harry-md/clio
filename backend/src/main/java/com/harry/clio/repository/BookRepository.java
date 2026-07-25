package com.harry.clio.repository;

import com.harry.clio.entity.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository
        extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @Query("""
        SELECT b
        FROM Book b
        JOIN FETCH b.categories c
        WHERE b.id = :bookId AND b.active = true AND b.type = 'SYSTEM'
        """)
    Optional<Book> findWithCategoryById(@Param(value = "bookId") Integer bookId);

    @Query("""
        SELECT b
        FROM Book b
        WHERE b.active = true AND b.status = 'COMPLETED' AND b.type = 'SYSTEM' AND b.id IN :bookIds
        """)
    List<Book> findAllPurchasableByIdIn(List<Integer> bookIds);
}
