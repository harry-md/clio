package com.harry.clio.repository;

import com.harry.clio.entity.Author;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AuthorRepository extends JpaRepository<Author, Integer> {
    long countByIdIn(Collection<Integer> id);
}
