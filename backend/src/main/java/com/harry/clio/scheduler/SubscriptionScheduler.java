package com.harry.clio.scheduler;

import com.harry.clio.service.SubscriptionService;

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
public class SubscriptionScheduler {
    private final SubscriptionService subscriptionService;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Scheduled(cron = "${clio.schedulers.daily-cron}", zone = "${clio.schedulers.zone-id}")
    public void expireSubscriptions() {
        int expiredSubs = subscriptionService.expireSubscriptions(LocalDate.now(ZoneId.of(zoneId)));
        log.info("Đã expire {} subscription", expiredSubs);
    }
}
