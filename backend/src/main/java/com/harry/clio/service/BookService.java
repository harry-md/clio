package com.harry.clio.service;

import com.harry.clio.dto.book.BookDetailResponse;
import com.harry.clio.dto.book.BookListResponse;
import com.harry.clio.dto.book.CreateBookMetadataRequest;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    BookDetailResponse uploadBook(
            int publisherId, CreateBookMetadataRequest request, MultipartFile file);

    Page<BookListResponse> getBooks();
}
