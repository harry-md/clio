package com.harry.clio.repository;

import com.harry.clio.model.BookInfo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookInfoRepository extends JpaRepository<BookInfo, Integer> {}
