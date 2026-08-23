package com.harry.clio.dto.stats;

import com.harry.clio.dto.publisher.PublisherDto;

import java.util.List;

public record PublisherDashboardResponse(
        PublisherDto publisher, List<TopSellingBookResponse> topSellingBooks) {}
