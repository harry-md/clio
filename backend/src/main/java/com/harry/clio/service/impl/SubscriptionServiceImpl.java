package com.harry.clio.service.impl;

import com.harry.clio.model.SubscriptionStatus;
import com.harry.clio.repository.SubscriptionRepository;
import com.harry.clio.service.SubscriptionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public int expireSubscriptions(LocalDate today) {
        return subscriptionRepository.expireSubscriptions(
                today, SubscriptionStatus.EXPIRED, SubscriptionStatus.ACTIVE);
    }
}
