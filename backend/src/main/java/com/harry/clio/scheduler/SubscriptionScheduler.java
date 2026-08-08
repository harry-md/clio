package com.harry.clio.scheduler;

import com.harry.clio.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class SubscriptionScheduler {
    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 1 0 * * *")
    public void expireSubscriptions() {
        int expiredSubs = subscriptionService.expireSubscriptions(LocalDate.now());
        log.info("Đã expire {} subscription", expiredSubs);
    }
}
