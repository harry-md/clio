package com.harry.clio.repository;

import com.harry.clio.entity.UserLibrary;
import com.harry.clio.entity.UserLibraryType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    Optional<UserLibrary> findByUserIdAndBookId(Integer userId, Integer bookId);
}
