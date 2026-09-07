package com.harry.clio.repository;

import com.harry.clio.dto.review.AdminReviewResponse;
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

    @Query(value = """
        SELECT new com.harry.clio.dto.review.AdminReviewResponse(r.id, b.id, b.title, u.id, u.username, r.rating, r.comment)
        FROM Review r
        JOIN r.book b
        JOIN r.user u
        WHERE LOWER(COALESCE(r.comment, '')) LIKE :keyword ESCAPE '!'
        ORDER BY r.id DESC
        """, countQuery = """
            SELECT COUNT(r)
            FROM Review r
            WHERE LOWER(COALESCE(r.comment, '')) LIKE :keyword ESCAPE '!'
            """)
    Page<AdminReviewResponse> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT r
        FROM Review r
        WHERE r.id = :id
        """)
    Optional<Review> findWithBookById(@Param("id") int id);
}
