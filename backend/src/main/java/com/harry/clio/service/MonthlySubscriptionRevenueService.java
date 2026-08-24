package com.harry.clio.service;

import com.harry.clio.model.MonthlySubscriptionRevenue;

import java.time.YearMonth;

public interface MonthlySubscriptionRevenueService {
    boolean checkThisMonthExist(YearMonth yearMonth);

    MonthlySubscriptionRevenue computeMonthlyRevenue(YearMonth yearMonth);
}
