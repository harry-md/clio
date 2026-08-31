package com.harry.clio.service;

import com.harry.clio.dto.library.ReadingProgressResponse;

public interface ReadingProgressService {
    ReadingProgressResponse getProgress(int userId, int bookId);

    ReadingProgressResponse updateProgress(int userId, int bookId, String cfiPosition);
}
