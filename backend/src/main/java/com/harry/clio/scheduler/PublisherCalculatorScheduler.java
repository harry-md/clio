package com.harry.clio.scheduler;

import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(
        prefix = "clio.schedulers",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PublisherCalculatorScheduler {
    private final PublisherService publisherService;

    @Value("${clio.schedulers.max-attempts}")
    private int maxAttempts;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Scheduled(cron = "${clio.schedulers.daily-cron}", zone = "${clio.schedulers.zone-id}")
    public void calculateTodayBookRevenue() {
        LocalDate today = LocalDate.now(ZoneId.of(zoneId));
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                publisherService.calculateBookRevenueToday(today);
                return;
            } catch (RuntimeException ex) {
                log.error(
                        "Tính doanh thu sách ngày {} cho NXB thất bại lần thứ {}/{}:",
                        today,
                        i,
                        maxAttempts,
                        ex);
                if (i == maxAttempts) {
                    log.error(
                            "Tính doanh thu sách ngày {} cho NXB thất bại sau {} lần thử",
                            today,
                            maxAttempts,
                            ex);
                }
            }
        }
    }
}
