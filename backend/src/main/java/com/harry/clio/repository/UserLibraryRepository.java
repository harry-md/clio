package com.harry.clio.repository;

import com.harry.clio.model.UserLibrary;
import com.harry.clio.model.UserLibraryType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserLibraryRepository extends JpaRepository<UserLibrary, Integer> {
    boolean existsByUserIdAndBookIdInAndType(
            Integer userId, List<Integer> bookIds, UserLibraryType type);

    @Query("""
        SELECT ul
        FROM UserLibrary ul
        JOIN FETCH ul.book
        WHERE ul.user.id = :userId AND ul.book.id = :bookId
        """)
    Optional<UserLibrary> findByUserIdAndBookId(
            @Param("userId") Integer userId, @Param("bookId") Integer bookId);

    List<UserLibrary> findAllByUserIdAndBookIdIn(Integer userId, List<Integer> bookIds);

    @Query(value = """
        SELECT ul
        FROM UserLibrary ul
        JOIN FETCH ul.book
        WHERE ul.user.id = :userId
        """, countQuery = """
            SELECT COUNT(ul)
            FROM UserLibrary ul
            WHERE ul.user.id = :userId
            """)
    Page<UserLibrary> findAllByUserId(@Param("userId") Integer userId, Pageable pageable);
}
