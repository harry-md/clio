package com.harry.clio.service.impl;

import com.harry.clio.dto.stats.*;
import com.harry.clio.dto.stats.PlatformRevenueResponse.RevenuePoint;
import com.harry.clio.dto.stats.PlatformRevenueResponse.TopBookRevenue;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.PublisherMapper;
import com.harry.clio.model.OrderStatus;
import com.harry.clio.model.Publisher;
import com.harry.clio.model.RevenueLogOwner;
import com.harry.clio.repository.PublisherRepository;
import com.harry.clio.repository.RevenueLogRepository;
import com.harry.clio.repository.StatRepository;
import com.harry.clio.service.StatService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;
    private final RevenueLogRepository revenueLogRepository;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    private record RevenuePeriod(
            boolean isMonth, LocalDate startDate, LocalDate endDate, Instant start, Instant end) {}

    @Override
    public PublisherDashboardResponse getPublisherDashboard(int publisherId, int year, int month) {
        if (year < 1 || month < 1 || month > 12) {
            throw new BadRequestException("Tháng năm không hợp lệ");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        Instant start = yearMonth.atDay(1).atStartOfDay(ZoneId.of(zoneId)).toInstant();
        Instant end =
                yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneId.of(zoneId)).toInstant();

        List<TopSellingBookResponse> books = statRepository.findTopSellingBooksByPublisherId(
                publisherId, OrderStatus.PAID, start, end, PageRequest.of(0, 5));

        Publisher publisher = publisherRepository
                .findById(publisherId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy NXB"));
        return new PublisherDashboardResponse(publisherMapper.toDto(publisher), books);
    }

    @Override
    public PlatformRevenueResponse getPlatformRevenue(
            RevenuePeriodType type, int period, int year, Pageable pageable) {
        RevenuePeriod revenuePeriod = getRevenuePeriod(type, period, year);

        Map<Integer, RevenuePointRow> rowsByBucket = loadRevenueByBucket(revenuePeriod);
        List<RevenuePoint> points = buildRevenuePoints(revenuePeriod, rowsByBucket);

        BigDecimal bookRevenue = points.stream()
                .map(RevenuePoint::bookRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subscriptionRevenue = points.stream()
                .map(RevenuePoint::subscriptionRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TopBookRevenue> topBooks = loadTopBooks(revenuePeriod, pageable);

        String label = (revenuePeriod.isMonth() ? "Tháng " : "Quý ") + period + "/" + year;

        return new PlatformRevenueResponse(
                revenuePeriod.isMonth() ? "MONTH" : "QUARTER",
                period,
                year,
                label,
                bookRevenue.add(subscriptionRevenue),
                bookRevenue,
                subscriptionRevenue,
                points,
                topBooks);
    }

    private RevenuePeriod getRevenuePeriod(RevenuePeriodType type, int period, int year) {
        if (year < 1) {
            throw new BadRequestException("Năm không hợp lệ.");
        }
        if (period < 1 || period > type.maxPeriod()) {
            throw new BadRequestException(
                    type.isMonth()
                            ? "Tháng phải nằm trong khoảng 1-12."
                            : "Quý phải nằm trong khoảng 1-4.");
        }

        int firstMonth = type.firstMonth(period);
        LocalDate startDate = LocalDate.of(year, firstMonth, 1);
        LocalDate endDate = startDate.plusMonths(type.monthsPerPeriod());
        return new RevenuePeriod(
                type.isMonth(),
                startDate,
                endDate,
                startDate.atStartOfDay(ZoneId.of(zoneId)).toInstant(),
                endDate.atStartOfDay(ZoneId.of(zoneId)).toInstant());
    }

    private Map<Integer, RevenuePointRow> loadRevenueByBucket(RevenuePeriod period) {
        List<RevenuePointRow> rows = revenueLogRepository.findPlatformRevenueDetails(
                RevenueLogOwner.PLATFORM,
                OrderStatus.PAID,
                period.isMonth() ? "day" : "month",
                period.start(),
                period.end(),
                zoneId);

        Map<Integer, RevenuePointRow> rowsByBucket = new HashMap<>();
        for (RevenuePointRow row : rows) {
            RevenuePointRow previous = rowsByBucket.putIfAbsent(row.bucket(), row);

            if (previous != null) {
                throw new IllegalStateException(
                        "Query thống kê trả về nhiều dòng cho bucket: " + row.bucket());
            }
        }

        return rowsByBucket;
    }

    private List<RevenuePoint> buildRevenuePoints(
            RevenuePeriod period, Map<Integer, RevenuePointRow> rowsByBucket) {
        List<RevenuePoint> points = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (LocalDate cursor = period.startDate();
                cursor.isBefore(period.endDate());
                cursor = period.isMonth() ? cursor.plusDays(1) : cursor.plusMonths(1)) {

            int bucket = period.isMonth() ? cursor.getDayOfMonth() : cursor.getMonthValue();

            RevenuePointRow row = rowsByBucket.get(bucket);

            String label = period.isMonth()
                    ? cursor.format(dayFormatter)
                    : "Tháng " + cursor.getMonthValue();

            points.add(new RevenuePoint(
                    label,
                    row == null ? BigDecimal.ZERO : row.bookRevenue(),
                    row == null ? BigDecimal.ZERO : row.subscriptionRevenue()));
        }
        return List.copyOf(points);
    }

    private List<TopBookRevenue> loadTopBooks(RevenuePeriod period, Pageable pageable) {
        return revenueLogRepository
                .findTopPlatformRevenueBooks(
                        RevenueLogOwner.PLATFORM,
                        OrderStatus.PAID,
                        period.start(),
                        period.end(),
                        pageable)
                .stream()
                .map(row ->
                        new TopBookRevenue(row.bookId(), row.title(), row.sales(), row.revenue()))
                .toList();
    }
}
