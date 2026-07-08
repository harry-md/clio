package com.harry.clio.service;

import com.harry.clio.dto.book.BookDetailResponse;
import com.harry.clio.dto.book.CreateBookRequest;

public interface BookService {
    BookDetailResponse uploadBook(int publisherId, CreateBookRequest request);
}
