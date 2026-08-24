package com.harry.clio.service.impl;

import com.harry.clio.model.MonthlySubscriptionRevenue;
import com.harry.clio.repository.MonthlySubscriptionRevenueRepository;
import com.harry.clio.repository.SubscriptionAllocationRepository;
import com.harry.clio.repository.SubscriptionBookBillingRepository;
import com.harry.clio.service.MonthlySubscriptionRevenueService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

@RequiredArgsConstructor
@Service
public class MonthlySubscriptionRevenueServiceImpl implements MonthlySubscriptionRevenueService {
    private final MonthlySubscriptionRevenueRepository monthlySubscriptionRevenueRepository;
    private final SubscriptionAllocationRepository allocationRepository;
    private final SubscriptionBookBillingRepository bookBillingRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public boolean checkThisMonthExist(YearMonth yearMonth) {
        return monthlySubscriptionRevenueRepository.existsByYearAndMonth(
                yearMonth.getYear(), yearMonth.getMonthValue());
    }

    @Override
    public MonthlySubscriptionRevenue computeMonthlyRevenue(YearMonth yearMonth) {
        BigDecimal totalPublisherRevenue =
                allocationRepository.findTotalPublisherAmountByMonthAndYearAndStatus(
                        yearMonth.getYear(), yearMonth.getMonthValue());

        Instant start = yearMonth.atDay(1).atStartOfDay(ZONE_ID).toInstant();
        Instant end = yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZONE_ID).toInstant();
        Long totalPageCount = bookBillingRepository.findMonthlyTotalPageCount(start, end);

        YearMonth prevYearMonth = yearMonth.minusMonths(1);
        MonthlySubscriptionRevenue prevMonthRevenue = monthlySubscriptionRevenueRepository
                .findByYearAndMonth(prevYearMonth.getYear(), prevYearMonth.getMonthValue())
                .orElse(null);

        BigDecimal finalPublisherAmount = prevMonthRevenue == null
                ? totalPublisherRevenue
                : totalPublisherRevenue.add(prevMonthRevenue.getUnallocatedAmount());

        BigDecimal unallocatedAmount = BigDecimal.ZERO;
        if (totalPageCount == 0) {
            unallocatedAmount = finalPublisherAmount;
            finalPublisherAmount = BigDecimal.ZERO;
        }

        return monthlySubscriptionRevenueRepository.save(MonthlySubscriptionRevenue.builder()
                .year(yearMonth.getYear())
                .month(yearMonth.getMonthValue())
                .totalPublisherAmount(totalPublisherRevenue)
                .unallocatedAmount(unallocatedAmount)
                .finalPublisherAmount(finalPublisherAmount)
                .totalPageCount(totalPageCount)
                .build());
    }
}
