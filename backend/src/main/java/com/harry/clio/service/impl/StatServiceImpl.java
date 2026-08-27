package com.harry.clio.service.impl;

import com.harry.clio.dto.stats.PublisherDashboardResponse;
import com.harry.clio.dto.stats.TopSellingBookResponse;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.PublisherMapper;
import com.harry.clio.model.DetailType;
import com.harry.clio.model.OrderStatus;
import com.harry.clio.model.Publisher;
import com.harry.clio.repository.PublisherRepository;
import com.harry.clio.repository.StatRepository;
import com.harry.clio.service.StatService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Value("${clio.schedulers.zone-id}")
    private String zoneId;

    @Override
    public PublisherDashboardResponse getPublisherDashboard(int publisherId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Instant start = yearMonth.atDay(1).atStartOfDay(ZoneId.of(zoneId)).toInstant();
        Instant end =
                yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZoneId.of(zoneId)).toInstant();

        List<TopSellingBookResponse> books = statRepository.findTopSellingBooksByPublisherId(
                publisherId, OrderStatus.PAID, DetailType.BOOK, start, end, PageRequest.of(0, 5));

        Publisher publisher = publisherRepository
                .findById(publisherId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy NXB"));
        return new PublisherDashboardResponse(publisherMapper.toDto(publisher), books);
    }
}
