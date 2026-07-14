package com.harry.clio.worker;

import com.harry.clio.service.BookProcessingQueue;
import com.harry.clio.service.BookProcessingService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class BookProcessingWorker implements SmartLifecycle {
    private static final Duration POP_TIMEOUT = Duration.ofSeconds(5);

    private final BookProcessingQueue bookProcessingQueue;
    private final BookProcessingService bookProcessingService;
    private final TaskExecutor taskExecutor;

    private volatile boolean running = false;

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        taskExecutor.execute(this::consumeLoop);
    }

    private void consumeLoop() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Integer bookId = bookProcessingQueue.dequeue(POP_TIMEOUT).orElse(null);
                if (bookId == null) {
                    continue;
                }
                bookProcessingService.process(bookId);
            } catch (RuntimeException e) {
                if (running) {
                    waitAndRetry();
                }
            }
        }
    }

    private void waitAndRetry() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
