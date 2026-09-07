package com.harry.clio.repository;

import com.harry.clio.model.BookAuthor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Integer> {
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM BookAuthor ba
        WHERE ba.book.id IN :bookIds
        """)
    int deleteByBookIds(@Param("bookIds") List<Integer> bookIds);
}
