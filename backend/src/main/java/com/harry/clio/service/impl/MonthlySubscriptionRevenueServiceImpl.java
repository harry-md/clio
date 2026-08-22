package com.harry.clio.service.impl;

import com.harry.clio.repository.MonthlySubscriptionRevenueRepository;
import com.harry.clio.service.MonthlySubscriptionRevenueService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MonthlySubscriptionRevenueServiceImpl implements MonthlySubscriptionRevenueService {
    private final MonthlySubscriptionRevenueRepository monthlySubscriptionRevenueRepository;

    @Override
    public void computeMonthlyRevenue(int month, int year) {}
}
