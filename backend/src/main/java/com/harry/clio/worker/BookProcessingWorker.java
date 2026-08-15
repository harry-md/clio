package com.harry.clio.worker;

import com.harry.clio.exception.InvalidEbookException;
import com.harry.clio.service.BookProcessingQueue;
import com.harry.clio.service.BookProcessingService;

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
public class BookProcessingWorker implements SmartLifecycle {
    @Value("${clio.book-workers}")
    private int bookWorkers;

    private final BookProcessingQueue bookProcessingQueue;
    private final BookProcessingService bookProcessingService;
    private final TaskExecutor bookProcessingExecutor;

    private static final Duration POP_TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

    private volatile boolean running = false;

    @Override
    public void start() {
        if (running) {
            return;
        }

        running = true;

        for (int i = 0; i < bookWorkers; i++) {
            bookProcessingExecutor.execute(this::consumeLoop);
        }
    }

    private void consumeLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            Integer bookId = bookProcessingQueue.dequeue(POP_TIMEOUT).orElse(null);
            if (bookId == null) {
                continue;
            }
            processWithRetries(bookId);
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

                bookProcessingService.process(bookId);
                return;
            } catch (InvalidEbookException ex) {
                bookProcessingService.handleBookFailed(bookId);
                return;
            } catch (RuntimeException ex) {
                log.error("Lỗi xử lý sách {}", bookId, ex);

                if (i == MAX_ATTEMPTS) {
                    bookProcessingService.handleBookFailed(bookId);
                    log.error("Sách {} đã xử lý thất bại", bookId, ex);
                    return;
                }

                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
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
