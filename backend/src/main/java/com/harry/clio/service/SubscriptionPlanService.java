package com.harry.clio.service;

import com.harry.clio.dto.subscription.SubscriptionPlanResponse;

import java.util.List;

public interface SubscriptionPlanService {
    List<SubscriptionPlanResponse> getSubscriptionPlans();
}
