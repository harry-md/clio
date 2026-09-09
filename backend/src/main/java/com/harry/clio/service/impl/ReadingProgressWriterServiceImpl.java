package com.harry.clio.service.impl;

import com.harry.clio.dto.library.PendingReadingProgress;
import com.harry.clio.service.ReadingProgressWriterService;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingProgressWriterServiceImpl implements ReadingProgressWriterService {
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 100;
    private static final String UPDATE_SQL = """
        UPDATE user_libraries
        SET cfi_position = ?, cfi_updated_at = ?, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = ? AND book_id = ? AND (cfi_updated_at IS NULL OR cfi_updated_at < ?)
        """;

    @Transactional
    public void updateBatch(List<PendingReadingProgress> progresses) {
        jdbcTemplate.batchUpdate(
                UPDATE_SQL,
                progresses,
                BATCH_SIZE,
                (PreparedStatement stm, PendingReadingProgress progress) -> {
                    Timestamp updatedAt = Timestamp.from(progress.updatedAt());
                    stm.setString(1, progress.cfiPosition());
                    stm.setTimestamp(2, updatedAt);
                    stm.setInt(3, progress.userId());
                    stm.setInt(4, progress.bookId());
                    stm.setTimestamp(5, updatedAt);
                });
    }

    @Transactional
    public void update(PendingReadingProgress progress) {
        Timestamp updatedAt = Timestamp.from(progress.updatedAt());
        jdbcTemplate.update(
                UPDATE_SQL,
                progress.cfiPosition(),
                updatedAt,
                progress.userId(),
                progress.bookId(),
                updatedAt);
    }
}
