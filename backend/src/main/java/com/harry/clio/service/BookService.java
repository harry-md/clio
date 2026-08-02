package com.harry.clio.service;

import com.harry.clio.dto.book.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    BookDetailResponse uploadBook(
            int publisherId, CreateBookMetadataRequest request, MultipartFile file);

    Page<BookListResponse> getAllBooks(BookFilterRequest request, Pageable pageable);

    BookDetailResponse getBookDetail(Integer bookId);

    DownloadResponse downloadBook(Integer userId, Integer bookId, DownloadRequest request);
}
