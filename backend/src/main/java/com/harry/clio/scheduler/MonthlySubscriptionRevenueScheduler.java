package com.harry.clio.scheduler;

import com.harry.clio.service.SubscriptionRevenueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@RequiredArgsConstructor
@Component
public class MonthlySubscriptionRevenueScheduler {
    private final SubscriptionRevenueService subscriptionRevenueService;

    private static final int MAX_ATTEMPTS = 3;

    @Scheduled(cron = "0 1 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void computeMonthlyRevenue() {
        // DEBUG: YearMonth yearMonth = YearMonth.now().minusMonths(1);
        YearMonth yearMonth = YearMonth.now();
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            try {
                subscriptionRevenueService.compute(yearMonth);
                return;
            } catch (Exception ex) {
                log.error(
                        "Tính doanh thu tháng {}/{} cho NXB bị thất bại lần thử {}/{}",
                        yearMonth.getMonthValue(),
                        yearMonth.getYear(),
                        i,
                        MAX_ATTEMPTS,
                        ex);

                if (i == MAX_ATTEMPTS) {
                    log.error(
                            "Tính doanh thu tháng {}/{} cho NXB bị thất bại sau {} lần thử",
                            yearMonth.getMonthValue(),
                            yearMonth.getYear(),
                            MAX_ATTEMPTS,
                            ex);
                }
            }
        }
    }
}
