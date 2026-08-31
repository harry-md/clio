package com.harry.clio.scheduler;

import com.harry.clio.service.progress.PendingReadingProgress;
import com.harry.clio.service.progress.ReadingProgressBatchWriter;
import com.harry.clio.service.progress.ReadingProgressBuffer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(
        prefix = "clio.schedulers",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ReadingProgressScheduler {
    private final ReadingProgressBuffer progressBuffer;
    private final ReadingProgressBatchWriter batchWriter;

    @Scheduled(fixedDelayString = "${clio.reading-progress.flush-delay}")
    public void flushReadingProgress() {
        List<PendingReadingProgress> snapshot;

        try {
            snapshot = progressBuffer.snapshot();
        } catch (DataAccessException ex) {
            log.error("Không đọc được reading progress từ Redis", ex);
            return;
        }

        if (snapshot.isEmpty()) {
            return;
        }

        try {
            batchWriter.writeBatch(snapshot);
        } catch (DataAccessException ex) {
            log.error("Không batch update được {} reading progress", snapshot.size(), ex);
            return;
        }

        int removed = 0;

        for (PendingReadingProgress progress : snapshot) {
            try {
                if (progressBuffer.removeIfUnchanged(progress)) {
                    removed++;
                }
            } catch (DataAccessException ex) {
                log.error(
                        "Không xóa được progress đã flush khỏi Redis " + "userId={}, bookId={}",
                        progress.userId(),
                        progress.bookId(),
                        ex);
            }
        }
        log.info("Đã flush {} reading progress, xóa {} Redis entries", snapshot.size(), removed);
    }
}
