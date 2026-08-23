package com.harry.clio.service;

import com.harry.clio.dto.stats.PublisherDashboardResponse;

public interface StatService {
    PublisherDashboardResponse getPublisherDashboard(int publisherId, int year, int month);
}
