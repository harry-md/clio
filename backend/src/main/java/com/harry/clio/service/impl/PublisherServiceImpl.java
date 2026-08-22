package com.harry.clio.service.impl;

import com.harry.clio.dto.publisher.AdminPublisherDto;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.dto.publisher.PublisherForm;
import com.harry.clio.dto.user.UserOption;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.PublisherMapper;
import com.harry.clio.mapper.UserMapper;
import com.harry.clio.model.*;
import com.harry.clio.repository.PublisherRepository;
import com.harry.clio.repository.RevenueLogRepository;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RevenueLogRepository revenueLogRepository;

    private Publisher getPublisherOrThrow(int userId) {
        return publisherRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy NXB"));
    }

    @Override
    public PublisherDto getPublisherByUserId(int userId) {
        return publisherMapper.toDto(getPublisherOrThrow(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<AdminPublisherDto> getAllPublishers() {
        return publisherRepository.findAllWithUser().stream()
                .map(publisher -> publisherMapper.toAdminDto(publisher.getUser(), publisher))
                .toList();
    }

    @Override
    public AdminPublisherDto getPublisherAdmin(int userId) {
        return publisherRepository
                .findWithUserByUserId(userId)
                .map(publisher -> publisherMapper.toAdminDto(publisher.getUser(), publisher))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy NXB"));
    }

    @Override
    public List<UserOption> getUserOptions() {
        return userRepository.findAllByRole(UserRole.READER).stream()
                .map(userMapper::toUserOption)
                .toList();
    }

    @Transactional
    @Override
    public void calculateBookRevenueToday(LocalDate today) {
        Instant startOfDay = today.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        List<RevenueLog> revenueLogs = revenueLogRepository.findAllWithPublisherBefore(
                false, startOfDay, RevenueLogOwner.PUBLISHER);

        Map<Integer, BigDecimal> revenueByPublisher = new HashMap<>();

        revenueLogs.forEach(log -> revenueByPublisher.merge(
                log.getPublisher().getUserId(), log.getAmount(), BigDecimal::add));

        revenueByPublisher.forEach((publisherId, revenue) -> {
            if (publisherRepository.increaseBalance(publisherId, revenue) != 1) {
                throw new RuntimeException("Lỗi khi cập nhật số dư NXB");
            }
        });

        revenueLogs.forEach(log -> log.setComputed(true));
    }

    @Transactional
    @Override
    public PublisherDto createPublisher(PublisherForm publisherForm) {
        if (publisherRepository.existsById(publisherForm.getUserId())) {
            throw new ResourceNotFoundException("NXB đã tồn tại");
        }

        User user = userRepository
                .findById(publisherForm.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setRole(UserRole.PUBLISHER);

        Publisher publisher = Publisher.builder()
                .user(user)
                .bankAccountNumber(publisherForm.getBankAccountNumber())
                .build();
        return publisherMapper.toDto(publisherRepository.save(publisher));
    }

    @Override
    public PublisherDto updatePublisher(int userId, PublisherForm publisherForm) {
        Publisher publisher = getPublisherOrThrow(userId);
        publisher.setBankAccountNumber(publisherForm.getBankAccountNumber());
        return publisherMapper.toDto(publisherRepository.save(publisher));
    }

    @Override
    public PublisherDto updatePublisher(int userId, PublisherDto dto) {
        Publisher publisher = getPublisherOrThrow(userId);
        publisher.setBankAccountNumber(dto.bankAccountNumber());
        return publisherMapper.toDto(publisherRepository.save(publisher));
    }
}
