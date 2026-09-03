package com.harry.clio.scheduler;

import com.harry.clio.service.SubscriptionRevenueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(
        prefix = "clio.schedulers",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class MonthlySubscriptionRevenueScheduler {
    private final SubscriptionRevenueService subscriptionRevenueService;

    @Value("${clio.schedulers.max-attempts}")
    private int maxAttempts;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Scheduled(cron = "${clio.schedulers.monthly-cron}", zone = "${clio.schedulers.zone-id}")
    public void computeMonthlyRevenue() {
        YearMonth yearMonth = YearMonth.now(ZoneId.of(zoneId)).minusMonths(1);
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                subscriptionRevenueService.compute(yearMonth);
                return;
            } catch (Exception ex) {
                log.error(
                        "Tính doanh thu tháng {}/{} cho NXB bị thất bại lần thử {}/{}",
                        yearMonth.getMonthValue(),
                        yearMonth.getYear(),
                        i,
                        maxAttempts,
                        ex);

                if (i == maxAttempts) {
                    log.error(
                            "Tính doanh thu tháng {}/{} cho NXB bị thất bại sau {} lần thử",
                            yearMonth.getMonthValue(),
                            yearMonth.getYear(),
                            maxAttempts,
                            ex);
                }
            }
        }
    }
}
