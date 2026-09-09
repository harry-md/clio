package com.harry.clio.scheduler;

import com.harry.clio.dto.library.PendingReadingProgress;
import com.harry.clio.infra.ReadingProgressHash;
import com.harry.clio.service.ReadingProgressWriterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "clio.schedulers",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ReadingProgressScheduler {
    private final ReadingProgressHash progressBuffer;
    private final ReadingProgressWriterService writer;

    @Scheduled(fixedDelayString = "${clio.reading-progress.flush-delay}")
    public void flushReadingProgress() {
        List<PendingReadingProgress> snapshot;

        try {
            snapshot = progressBuffer.snapshot();
        } catch (DataAccessException ex) {
            log.error("Không đọc được reading progress", ex);
            return;
        }

        if (snapshot.isEmpty()) {
            return;
        }

        try {
            writer.updateBatch(snapshot);
        } catch (DataAccessException ex) {
            log.error("Không batch update được {} reading progress", snapshot.size(), ex);
            return;
        }

        int removed = 0;
        for (PendingReadingProgress progress : snapshot) {
            if (progressBuffer.removeIfUnchanged(progress)) {
                removed++;
            }
        }
        log.info("Đã lưu xuống {} reading progress, xóa {} progresses", snapshot.size(), removed);
    }
}
