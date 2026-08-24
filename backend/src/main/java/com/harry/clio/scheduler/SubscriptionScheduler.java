package com.harry.clio.scheduler;

import com.harry.clio.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
public class SubscriptionScheduler {
    private final SubscriptionService subscriptionService;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void expireSubscriptions() {
        int expiredSubs = subscriptionService.expireSubscriptions(LocalDate.now(ZONE_ID));
        log.info("Đã expire {} subscription", expiredSubs);
    }
}
