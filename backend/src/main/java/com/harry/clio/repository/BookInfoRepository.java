package com.harry.clio.repository;

import com.harry.clio.model.BookInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface BookInfoRepository extends JpaRepository<BookInfo, Integer> {
    @Transactional
    @Modifying
    @Query("""
        UPDATE BookInfo bi
        SET bi.fileSize = :fileSize, bi.wordCount = :wordCount
        WHERE bi.bookId = :id
        """)
    int updateInfo(
            @Param("id") int id,
            @Param("fileSize") long fileSize,
            @Param("wordCount") long wordCount);
}
