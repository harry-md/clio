package com.harry.clio.worker;

import com.harry.clio.config.properties.BookWorkerProperties;
import com.harry.clio.exception.InvalidEbookException;
import com.harry.clio.queue.BookQueue;
import com.harry.clio.service.BookProcessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookWorker implements SmartLifecycle {
    private final BookWorkerProperties workerProps;
    private final BookQueue bookQueue;
    private final BookProcessService bookProcessService;
    private final TaskExecutor bookExecutor;

    private volatile boolean running = false;

    @Override
    public void start() {
        if (running) {
            return;
        }

        int jobs = bookQueue.recoverJobs();
        log.info("Recover {} jobs bị crash", jobs);

        running = true;

        for (int i = 0; i < workerProps.count(); i++) {
            bookExecutor.execute(this::consumeLoop);
        }
    }

    private void consumeLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Integer bookId = bookQueue.claimJob(workerProps.timeout()).orElse(null);
                if (bookId == null) {
                    continue;
                }
                processWithRetries(bookId);
            } catch (RuntimeException | Error ex) {
                log.error("Lỗi xử lý sách", ex);
            }
        }
    }

    private void processWithRetries(int bookId) {
        for (int i = 1; i <= workerProps.maxAttempts(); i++) {
            try {
                log.info(
                        "Xử lý sách {}, lần {}/{}, worker {}",
                        bookId,
                        i,
                        workerProps.maxAttempts(),
                        Thread.currentThread().getName());

                bookProcessService.process(bookId);
                break;
            } catch (InvalidEbookException ex) {
                bookProcessService.handleBookFailed(bookId);
                remove(bookId);
                return;
            } catch (RuntimeException ex) {
                log.error("Lỗi xử lý sách {}", bookId, ex);

                if (i == workerProps.maxAttempts()) {
                    bookProcessService.handleBookFailed(bookId);
                    remove(bookId);
                    log.error("Sách {} đã xử lý thất bại", bookId, ex);
                    return;
                }

                try {
                    Thread.sleep(workerProps.retryDelay());
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        remove(bookId);
    }

    private void remove(int bookId) {
        if (!bookQueue.removeJob(bookId)) {
            log.error("Xóa job khỏi process queue thất bại");
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
