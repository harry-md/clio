package com.harry.clio.service.impl;

import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.entity.Publisher;
import com.harry.clio.entity.User;
import com.harry.clio.entity.UserRole;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.PublisherMapper;
import com.harry.clio.repository.PublisherRepository;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;
    private final UserRepository userRepository;

    private Publisher getPublisherOrThrow(int userId) {
        return publisherRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản"));
    }

    @Override
    public PublisherDto getPublisherByUserId(int userId) {
        return publisherMapper.toDto(getPublisherOrThrow(userId));
    }

    @Override
    public List<PublisherDto> getAllPublishers() {
        return publisherRepository.findAll().stream()
                .map(publisherMapper::toDto)
                .toList();
    }

    @Override
    public PublisherDto createPublisher(PublisherDto publisherDto) {
        if (publisherRepository.existsById(publisherDto.userId())) {
            throw new ResourceNotFoundException("Nhà xuất bản đã tồn tại");
        }

        User user = userRepository
                .findById(publisherDto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setRole(UserRole.PUBLISHER);

        Publisher publisher = Publisher.builder()
                .user(user)
                .bankAccountNumber(publisherDto.bankAccountNumber())
                .build();
        return publisherMapper.toDto(publisherRepository.save(publisher));
    }

    @Override
    public PublisherDto updatePublisher(int userId, PublisherDto publisherDto) {
        Publisher publisher = getPublisherOrThrow(userId);
        publisher.setBankAccountNumber(publisherDto.bankAccountNumber());
        return publisherMapper.toDto(publisherRepository.save(publisher));
    }
}
