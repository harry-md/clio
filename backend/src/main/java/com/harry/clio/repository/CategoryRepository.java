package com.harry.clio.repository;

import com.harry.clio.model.Category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    long countByIdIn(Collection<Integer> id);
}
