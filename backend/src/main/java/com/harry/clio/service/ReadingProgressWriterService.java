package com.harry.clio.service;

import com.harry.clio.dto.library.PendingReadingProgress;

import java.util.List;

public interface ReadingProgressWriterService {
    void updateBatch(List<PendingReadingProgress> progresses);

    void update(PendingReadingProgress progress);
}
