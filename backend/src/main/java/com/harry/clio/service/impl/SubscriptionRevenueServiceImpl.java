package com.harry.clio.service.impl;

import com.harry.clio.model.MonthlySubscriptionRevenue;
import com.harry.clio.service.MonthlySubscriptionRevenueService;
import com.harry.clio.service.PublisherMonthlySubscriptionRevenueService;
import com.harry.clio.service.SubscriptionRevenueService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@RequiredArgsConstructor
@Service
public class SubscriptionRevenueServiceImpl implements SubscriptionRevenueService {
    private final MonthlySubscriptionRevenueService monthlySubRevenueService;
    private final PublisherMonthlySubscriptionRevenueService publisherSubService;

    @Transactional
    @Override
    public void compute(YearMonth yearMonth) {
        if (monthlySubRevenueService.checkThisMonthExist(yearMonth)) {
            return;
        }

        MonthlySubscriptionRevenue monthlyRevenue =
                monthlySubRevenueService.computeMonthlyRevenue(yearMonth);

        publisherSubService.computePublisherMonthlyRevenue(monthlyRevenue);
    }
}
