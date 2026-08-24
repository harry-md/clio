package com.harry.clio.scheduler;

import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
public class PublisherCalculatorScheduler {
    private final PublisherService publisherService;

    private static final int MAX_ATTEMPTS = 3;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void calculateTodayBookRevenue() {
        LocalDate today = LocalDate.now(ZONE_ID);
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try {
                publisherService.calculateBookRevenueToday(today);
                return;
            } catch (RuntimeException ex) {
                log.error(
                        "Tính doanh thu sách ngày {} cho NXB thất bại lần thứ {}/{}:",
                        today,
                        i,
                        MAX_ATTEMPTS,
                        ex);
                if (i == MAX_ATTEMPTS) {
                    log.error(
                            "Tính doanh thu sách ngày {} cho NXB thất bại sau {} lần thử",
                            today,
                            MAX_ATTEMPTS,
                            ex);
                }
            }
        }
    }
}
