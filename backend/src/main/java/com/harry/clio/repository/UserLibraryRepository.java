package com.harry.clio.repository;

import com.harry.clio.entity.UserLibrary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLibraryRepository extends JpaRepository<UserLibrary, Integer> {
    boolean existsByUserIdAndBookIdIn(Integer userId, List<Integer> bookIds);

    Optional<UserLibrary> findByUserIdAndBookId(Integer userId, Integer bookId);
}
