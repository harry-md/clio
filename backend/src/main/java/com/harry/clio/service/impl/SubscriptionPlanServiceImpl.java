package com.harry.clio.service.impl;

import com.harry.clio.dto.subscription.SubscriptionPlanResponse;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.SubscriptionPlanMapper;
import com.harry.clio.repository.SubscriptionPlanRepository;
import com.harry.clio.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public List<SubscriptionPlanResponse> getSubscriptionPlans() {
        return subscriptionPlanRepository.findAllByActiveTrue().stream()
                .map(subscriptionPlanMapper::toDto)
                .toList();
    }

    @Override
    public SubscriptionPlanResponse getDetailSubscriptionPlan(Integer subscriptionPlanId) {
        return subscriptionPlanMapper.toDto(subscriptionPlanRepository
                .findByIdAndActiveTrue(subscriptionPlanId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy subscription plan")));
    }
}
