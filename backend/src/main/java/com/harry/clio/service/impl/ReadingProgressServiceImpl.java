package com.harry.clio.service.impl;

import com.harry.clio.dto.library.ReadingProgressResponse;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.model.UserLibrary;
import com.harry.clio.repository.UserLibraryRepository;
import com.harry.clio.service.ReadingProgressService;
import com.harry.clio.service.progress.PendingReadingProgress;
import com.harry.clio.service.progress.ReadingProgressBatchWriter;
import com.harry.clio.service.progress.ReadingProgressBuffer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReadingProgressServiceImpl implements ReadingProgressService {

    private final UserLibraryRepository userLibraryRepository;
    private final ReadingProgressBuffer progressBuffer;
    private final ReadingProgressBatchWriter batchWriter;

    @Override
    public ReadingProgressResponse getProgress(int userId, int bookId) {
        try {
            Optional<PendingReadingProgress> pendingProgress = progressBuffer.find(userId, bookId);
            if (pendingProgress.isPresent()) {
                return new ReadingProgressResponse(pendingProgress.get().cfiPosition());
            }
        } catch (DataAccessException ex) {
            log.warn("Redis lỗi khi đọc progress userId={}, bookId={}", userId, bookId, ex);
        }

        UserLibrary library = userLibraryRepository
                .findWithBookByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new BadRequestException("Sách không có trong thư viện"));
        return new ReadingProgressResponse(library.getCfiPosition());
    }

    @Override
    public ReadingProgressResponse updateProgress(int userId, int bookId, String cfiPosition) {
        if (!userLibraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new BadRequestException("Sách không có trong thư viện");
        }

        PendingReadingProgress progress =
                PendingReadingProgress.create(userId, bookId, cfiPosition);

        try {
            progressBuffer.put(progress);
        } catch (DataAccessException ex) {
            log.warn(
                    "Redis lỗi, fallback ghi progress trực tiếp xuống DB " + "userId={}, bookId={}",
                    userId,
                    bookId,
                    ex);

            batchWriter.writeOne(progress);
        }
        return new ReadingProgressResponse(cfiPosition);
    }
}
