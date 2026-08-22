package com.harry.clio.scheduler;

import com.harry.clio.service.BookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BookScheduler {
    private final BookService bookService;

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void deleteFailedBooks() {
        int deletedBooks = bookService.deleteFailedBooks();
        log.info("Đã xóa {} sách upload thất bại", deletedBooks);
    }
}
