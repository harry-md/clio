package com.harry.clio.service.impl;

import com.harry.clio.model.MonthlySubscriptionRevenue;
import com.harry.clio.model.PublisherMonthlySubscriptionRevenue;
import com.harry.clio.model.SubscriptionBookBilling;
import com.harry.clio.repository.PublisherMonthlySubscriptionRevenueRepository;
import com.harry.clio.repository.PublisherRepository;
import com.harry.clio.repository.SubscriptionBookBillingRepository;
import com.harry.clio.service.PublisherMonthlySubscriptionRevenueService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@RequiredArgsConstructor
@Service
public class PublisherMonthlySubscriptionRevenueServiceImpl
        implements PublisherMonthlySubscriptionRevenueService {
    private final PublisherMonthlySubscriptionRevenueRepository revenueRepository;
    private final SubscriptionBookBillingRepository billingRepository;
    private final PublisherRepository publisherRepository;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Override
    public void computePublisherMonthlyRevenue(
            MonthlySubscriptionRevenue monthlySubscriptionRevenue) {
        YearMonth yearMonth = YearMonth.of(
                monthlySubscriptionRevenue.getYear(), monthlySubscriptionRevenue.getMonth());
        Instant start = yearMonth.atDay(1).atStartOfDay(ZoneId.of(zoneId)).toInstant();
        Instant end =
                yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneId.of(zoneId)).toInstant();

        List<SubscriptionBookBilling> billings =
                billingRepository.findAllWithBookInMonth(start, end);
        Map<Integer, Long> billingByPublisher = new HashMap<>();
        billings.forEach(billing -> {
            billingByPublisher.merge(
                    billing.getBook().getPublisher().getUserId(),
                    billing.getPageCount(),
                    Long::sum);
        });

        Set<PublisherMonthlySubscriptionRevenue> pubMonthlyRevenues = new HashSet<>();
        billingByPublisher.forEach((publisherId, pageCount) -> {
            BigDecimal amount = monthlySubscriptionRevenue
                    .getFinalPublisherAmount()
                    .multiply(BigDecimal.valueOf(pageCount))
                    .divide(
                            BigDecimal.valueOf(monthlySubscriptionRevenue.getTotalPageCount()),
                            2,
                            RoundingMode.HALF_UP);

            pubMonthlyRevenues.add(PublisherMonthlySubscriptionRevenue.builder()
                    .publisher(publisherRepository.getReferenceById(publisherId))
                    .monthlySubscriptionRevenue(monthlySubscriptionRevenue)
                    .pageCount(pageCount)
                    .amount(amount)
                    .build());
        });
        revenueRepository.saveAll(pubMonthlyRevenues);

        pubMonthlyRevenues.forEach(pubMonthlyRevenue -> {
            int res = publisherRepository.increaseBalance(
                    pubMonthlyRevenue.getPublisher().getUserId(), pubMonthlyRevenue.getAmount());
            if (res != 1) {
                throw new RuntimeException("Có lỗi khi cộng số dư NXB");
            }
        });
    }
}
