package com.harry.clio.service.progress;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReadingProgressBatchWriter {
    private final JdbcTemplate jdbcTemplate;

    private static final String UPDATE_SQL = """
        UPDATE user_libraries
        SET cfi_position = ?,
            cfi_updated_at = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = ?
          AND book_id = ?
          AND (
              cfi_updated_at IS NULL
              OR cfi_updated_at < ?
          )
        """;
    private static final int BATCH_SIZE = 100;

    @Transactional
    public void writeBatch(List<PendingReadingProgress> progressList) {
        jdbcTemplate.batchUpdate(UPDATE_SQL, progressList, BATCH_SIZE, this::bindParameters);
    }

    @Transactional
    public void writeOne(PendingReadingProgress progress) {
        Timestamp updatedAt = Timestamp.from(progress.updatedAt());

        jdbcTemplate.update(
                UPDATE_SQL,
                progress.cfiPosition(),
                updatedAt,
                progress.userId(),
                progress.bookId(),
                updatedAt);
    }

    private void bindParameters(PreparedStatement statement, PendingReadingProgress progress)
            throws SQLException {
        Timestamp updatedAt = Timestamp.from(progress.updatedAt());
        statement.setString(1, progress.cfiPosition());
        statement.setTimestamp(2, updatedAt);
        statement.setInt(3, progress.userId());
        statement.setInt(4, progress.bookId());
        statement.setTimestamp(5, updatedAt);
    }
}
