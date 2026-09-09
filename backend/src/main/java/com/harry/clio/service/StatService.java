package com.harry.clio.service;

import com.harry.clio.dto.stats.PlatformRevenueResponse;
import com.harry.clio.dto.stats.PublisherDashboardResponse;
import com.harry.clio.dto.stats.RevenuePeriodType;

import org.springframework.data.domain.Pageable;

public interface StatService {
    PublisherDashboardResponse getPublisherDashboard(int publisherId, int year, int month);

    PlatformRevenueResponse getPlatformRevenue(
            RevenuePeriodType type, int period, int year, Pageable pageable);
}
