package com.harry.clio.service;

import java.time.LocalDate;

public interface SubscriptionService {
    int expireSubscriptions(LocalDate today);
}
