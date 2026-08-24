package com.harry.clio.worker;

import com.harry.clio.exception.InvalidEbookException;
import com.harry.clio.queue.BookQueue;
import com.harry.clio.service.BookProcessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookWorker implements SmartLifecycle {
    @Value("${clio.book-workers}")
    private int bookWorkers;

    private final BookQueue bookQueue;
    private final BookProcessService bookProcessService;
    private final TaskExecutor bookExecutor;

    private static final Duration POP_TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);
    private volatile boolean running = false;

    @Override
    public void start() {
        if (running) {
            return;
        }

        int jobs = bookQueue.recoverProcessingJobs();
        log.info("Recover {} jobs bị crash", jobs);

        running = true;

        for (int i = 0; i < bookWorkers; i++) {
            bookExecutor.execute(this::consumeLoop);
        }
    }

    private void consumeLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Integer bookId = bookQueue.claim(POP_TIMEOUT).orElse(null);
                if (bookId == null) {
                    continue;
                }
                processWithRetries(bookId);
            } catch (RuntimeException ex) {
                log.error("Lỗi xử lý sách", ex);
            }
        }
    }

    private void processWithRetries(int bookId) {
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try {
                log.info(
                        "Xử lý sách {}, lần {}/{}, worker {}",
                        bookId,
                        i,
                        MAX_ATTEMPTS,
                        Thread.currentThread().getName());

                bookProcessService.process(bookId);
                break;
            } catch (InvalidEbookException ex) {
                bookProcessService.handleBookFailed(bookId);
                ack(bookId);
                return;
            } catch (RuntimeException ex) {
                log.error("Lỗi xử lý sách {}", bookId, ex);

                if (i == MAX_ATTEMPTS) {
                    bookProcessService.handleBookFailed(bookId);
                    ack(bookId);
                    log.error("Sách {} đã xử lý thất bại", bookId, ex);
                    return;
                }

                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        ack(bookId);
    }

    private void ack(int bookId) {
        if (!bookQueue.acknowledge(bookId)) {
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
