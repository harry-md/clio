package com.harry.clio.repository;

import com.harry.clio.entity.UserLibrary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLibraryRepository extends JpaRepository<UserLibrary, Integer> {
    boolean existsByUserIdAndBookIdIn(int userId, List<Integer> bookIds);
}
