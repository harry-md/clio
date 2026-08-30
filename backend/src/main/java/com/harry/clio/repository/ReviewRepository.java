package com.harry.clio.repository;

import com.harry.clio.model.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("""
        SELECT r
        FROM Review r
        JOIN FETCH r.user u
        WHERE u.id = :userId AND r.book.id = :bookId
        """)
    Optional<Review> findWithUserByUserIdAndBookId(
            @Param("userId") int userId, @Param("bookId") int bookId);

    boolean existsByUserIdAndBookId(int userId, int bookId);

    @EntityGraph(attributePaths = "user")
    @Transactional(readOnly = true)
    Page<Review> findAllByBookId(int bookId, Pageable pageable);
}
