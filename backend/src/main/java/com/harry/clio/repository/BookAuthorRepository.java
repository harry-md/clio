package com.harry.clio.repository;

import com.harry.clio.model.BookAuthor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Integer> {}
