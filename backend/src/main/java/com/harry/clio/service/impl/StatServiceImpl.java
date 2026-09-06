package com.harry.clio.service.impl;

import com.harry.clio.dto.stats.PlatformRevenueResponse;
import com.harry.clio.dto.stats.PlatformRevenueResponse.RevenuePoint;
import com.harry.clio.dto.stats.PlatformRevenueResponse.TopBookRevenue;
import com.harry.clio.dto.stats.PublisherDashboardResponse;
import com.harry.clio.dto.stats.TopSellingBookResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;
    private final RevenueLogRepository revenueLogRepository;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Override
    public PublisherDashboardResponse getPublisherDashboard(int publisherId, int year, int month) {

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
    @Transactional(readOnly = true)
    public PlatformRevenueResponse getPlatformRevenue(String time, int period, int year) {
        String normalizedTime = time == null ? "MONTH" : time.strip().toUpperCase(Locale.ROOT);

        if (!normalizedTime.equals("MONTH") && !normalizedTime.equals("QUARTER")) {
            throw new BadRequestException("Kiểu thống kê phải là MONTH hoặc QUARTER.");
        }

        boolean monthly = normalizedTime.equals("MONTH");
        int maxPeriod = monthly ? 12 : 4;

        if (period < 1 || period > maxPeriod) {
            throw new BadRequestException(
                    monthly
                            ? "Tháng phải nằm trong khoảng 1–12."
                            : "Quý phải nằm trong khoảng 1–4.");
        }

        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Năm phải nằm trong khoảng 2000–2100.");
        }

        int firstMonth = monthly ? period : (period - 1) * 3 + 1;

        LocalDate startDate = LocalDate.of(year, firstMonth, 1);
        LocalDate endDate = startDate.plusMonths(monthly ? 1 : 3);

        ZoneId zone = ZoneId.of(zoneId);
        Instant start = startDate.atStartOfDay(zone).toInstant();
        Instant end = endDate.atStartOfDay(zone).toInstant();

        String owner = RevenueLogOwner.PLATFORM.name();
        String status = OrderStatus.PAID.name();

        String unit = monthly ? "day" : "month";

        Map<Integer, RevenueLogRepository.RevenuePointRow> rowsByBucket = new HashMap<>();

        for (RevenueLogRepository.RevenuePointRow row :
                revenueLogRepository.findPlatformRevenueDetails(
                        owner, status, unit, start, end, zoneId)) {
            rowsByBucket.put(row.getBucket(), row);
        }

        List<RevenuePoint> points = new ArrayList<>();
        BigDecimal bookRevenue = BigDecimal.ZERO;
        BigDecimal subscriptionRevenue = BigDecimal.ZERO;

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (LocalDate cursor = startDate;
                cursor.isBefore(endDate);
                cursor = monthly ? cursor.plusDays(1) : cursor.plusMonths(1)) {

            int bucket = monthly ? cursor.getDayOfMonth() : cursor.getMonthValue();

            RevenueLogRepository.RevenuePointRow row = rowsByBucket.get(bucket);

            BigDecimal bookAmount = row == null ? BigDecimal.ZERO : row.getBookRevenue();

            BigDecimal subscriptionAmount =
                    row == null ? BigDecimal.ZERO : row.getSubscriptionRevenue();

            String pointLabel =
                    monthly ? cursor.format(dayFormatter) : "Tháng " + cursor.getMonthValue();

            points.add(new RevenuePoint(pointLabel, bookAmount, subscriptionAmount));

            bookRevenue = bookRevenue.add(bookAmount);
            subscriptionRevenue = subscriptionRevenue.add(subscriptionAmount);
        }

        List<TopBookRevenue> topBooks =
                revenueLogRepository.findTopPlatformRevenueBooks(owner, status, start, end).stream()
                        .map(row -> new TopBookRevenue(
                                row.getBookId(), row.getTitle(), row.getSales(), row.getRevenue()))
                        .toList();

        String label = (monthly ? "Tháng " : "Quý ") + period + "/" + year;

        return new PlatformRevenueResponse(
                normalizedTime,
                period,
                year,
                label,
                bookRevenue.add(subscriptionRevenue),
                bookRevenue,
                subscriptionRevenue,
                List.copyOf(points),
                topBooks);
    }
}
