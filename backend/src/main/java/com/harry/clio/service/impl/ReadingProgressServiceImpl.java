package com.harry.clio.service.impl;

import com.harry.clio.dto.library.PendingReadingProgress;
import com.harry.clio.dto.library.ReadingProgressDto;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.infra.ReadingProgressHash;
import com.harry.clio.model.UserLibrary;
import com.harry.clio.repository.UserLibraryRepository;
import com.harry.clio.service.ReadingProgressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingProgressServiceImpl implements ReadingProgressService {
    private final UserLibraryRepository userLibraryRepository;
    private final ReadingProgressHash progressBuffer;
    private final ReadingProgressWriterServiceImpl batchWriter;

    @Override
    public ReadingProgressDto getProgress(int userId, int bookId) {
        try {
            Optional<PendingReadingProgress> pendingProgress = progressBuffer.get(userId, bookId);
            if (pendingProgress.isPresent()) {
                return new ReadingProgressDto(pendingProgress.get().cfiPosition());
            }
        } catch (RuntimeException ex) {
            log.error("Lỗi khi đọc progress user {} book {}", userId, bookId, ex);
        }

        UserLibrary library = userLibraryRepository
                .findWithBookByUserIdAndBookId(userId, bookId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy sách trong thư viện"));
        return new ReadingProgressDto(library.getCfiPosition());
    }

    @Override
    public ReadingProgressDto updateProgress(int userId, int bookId, String cfiPosition) {
        if (!userLibraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException("Không tìm thấy sách trong thư viện");
        }

        PendingReadingProgress progress =
                PendingReadingProgress.create(userId, bookId, cfiPosition);
        try {
            progressBuffer.put(progress);
        } catch (DataAccessException ex) {
            batchWriter.update(progress);
        }
        return new ReadingProgressDto(cfiPosition);
    }
}
