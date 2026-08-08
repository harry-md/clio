package com.harry.clio.service;

import com.harry.clio.dto.book.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDetailResponse uploadBook(int publisherId, CreateBookMetadataRequest request);

    Page<BookListResponse> getAllBooks(BookFilterRequest request, Pageable pageable);

    BookDetailResponse getBookDetail(int bookId);

    int deleteFailedBooks();
}
