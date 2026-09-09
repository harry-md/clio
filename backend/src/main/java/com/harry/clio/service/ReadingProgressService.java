package com.harry.clio.service;

import com.harry.clio.dto.library.ReadingProgressDto;

public interface ReadingProgressService {
    ReadingProgressDto getProgress(int userId, int bookId);

    ReadingProgressDto updateProgress(int userId, int bookId, String cfiPosition);
}
