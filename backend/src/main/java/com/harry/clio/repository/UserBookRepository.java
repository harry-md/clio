package com.harry.clio.repository;

import com.harry.clio.entity.UserBook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBookRepository extends JpaRepository<UserBook, Integer> {
    boolean existsByUserIdAndBookIdIn(int userId, List<Integer> bookIds);
}
