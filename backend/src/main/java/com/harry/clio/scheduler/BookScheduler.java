package com.harry.clio.scheduler;

import com.harry.clio.service.BookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(
        prefix = "clio.schedulers",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class BookScheduler {
    private final BookService bookService;

    @Scheduled(cron = "${clio.schedulers.daily-cron}", zone = "${clio.schedulers.zone-id}")
    public void deleteFailedBooks() {
        int deletedBooks = bookService.deleteFailedBooks();
        log.info("Đã xóa {} sách upload thất bại", deletedBooks);
    }
}
