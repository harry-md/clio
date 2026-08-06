package com.harry.clio.scheduler;

import com.harry.clio.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class SubscriptionScheduler {
    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 1 0 * * *")
    public void expireSubscriptions() {
        subscriptionService.expireSubscriptions(LocalDate.now());
    }
}
