package com.harry.clio.scheduler;

import com.harry.clio.service.MonthlySubscriptionRevenueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
public class MonthlySubscriptionRevenueScheduler {
    private final MonthlySubscriptionRevenueService monthlySubscriptionRevenueService;
    private static final int MAX_ATTEMPTS = 3;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    @Scheduled(cron = "0 1 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void computeMonthlyRevenue() {
        LocalDate now = LocalDate.now(ZONE_ID);
        int month = now.getMonthValue(), year = now.getYear();

        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try {
                monthlySubscriptionRevenueService.computeMonthlyRevenue(month, year);
                break;
            } catch (Exception ex) {
                log.error(
                        "Tính doanh thu tháng {}/{} cho NXB bị thất bại lần thứ {}/{}",
                        month,
                        year,
                        i,
                        MAX_ATTEMPTS,
                        ex);

                if (i == MAX_ATTEMPTS) {
                    log.error(
                            "Tính doanh thu tháng {}/{} cho NXB bị thất bại sau {} lần thử",
                            month,
                            year,
                            MAX_ATTEMPTS,
                            ex);
                }
            }
        }
    }
}
