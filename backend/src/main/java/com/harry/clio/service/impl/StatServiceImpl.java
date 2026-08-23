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

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StatServiceImpl implements StatService {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    private final StatRepository statRepository;
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Override
    public PublisherDashboardResponse getPublisherDashboard(int publisherId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Instant startDate = yearMonth.atDay(1).atStartOfDay(ZONE_ID).toInstant();
        Instant endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZONE_ID).toInstant();

        List<TopSellingBookResponse> books = statRepository.findTopSellingBooksByPublisherId(
                publisherId,
                OrderStatus.PAID,
                DetailType.BOOK,
                startDate,
                endDate,
                PageRequest.of(0, 5));

        Publisher publisher = publisherRepository
                .findById(publisherId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy NXB"));
        return new PublisherDashboardResponse(publisherMapper.toDto(publisher), books);
    }
}
