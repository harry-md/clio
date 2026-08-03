package com.harry.clio.service;

import com.harry.clio.dto.book.DownloadRequest;
import com.harry.clio.dto.book.DownloadResponse;
import com.harry.clio.dto.library.LibraryResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserLibraryService {
    LibraryResponse addToLibrary(Integer userId, Integer bookId);

    Page<LibraryResponse> getUserLibraries(int userId, Pageable pageable);

    DownloadResponse downloadBook(int userId, DownloadRequest request);
}
