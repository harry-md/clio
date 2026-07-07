package com.harry.clio.service.impl;

import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.entity.Publisher;
import com.harry.clio.entity.User;
import com.harry.clio.entity.UserRole;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.DuplicateResourceException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.PublisherMapper;
import com.harry.clio.repository.PublisherRepository;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
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
    @Transactional(readOnly = true)
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
        User user = userRepository
                .findById(publisherDto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (publisherRepository.existsById(publisherDto.userId())) {
            throw new DuplicateResourceException("Nhà xuất bản đã tồn tại");
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("User đang là ADMIN");
        }
        user.setRole(UserRole.PUBLISHER);

        Publisher publisher = publisherMapper.toEntity(publisherDto);
        publisher.setUser(user);
        return publisherMapper.toDto(publisherRepository.save(publisher));
    }

    @Override
    public PublisherDto updatePublisher(int userId, PublisherDto publisherDto) {
        Publisher publisher = getPublisherOrThrow(userId);
        publisher.setBankAccountNumber(publisherDto.bankAccountNumber());
        return publisherMapper.toDto(publisherRepository.save(publisher));
    }
}
