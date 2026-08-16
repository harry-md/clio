package com.harry.clio.service.impl;

import com.harry.clio.dto.publisher.PublisherAdminDto;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.dto.publisher.PublisherForm;
import com.harry.clio.dto.user.UserOption;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.PublisherMapper;
import com.harry.clio.mapper.UserMapper;
import com.harry.clio.model.Publisher;
import com.harry.clio.model.User;
import com.harry.clio.model.UserRole;
import com.harry.clio.repository.PublisherRepository;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.service.PublisherService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private Publisher getPublisherOrThrow(int userId) {
        return publisherRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản"));
    }

    @Override
    public PublisherDto getPublisherByUserId(int userId) {
        return publisherMapper.toDto(getPublisherOrThrow(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<PublisherAdminDto> getAllPublishers() {
        return publisherRepository.findAllWithUser().stream()
                .map(publisher -> publisherMapper.toAdminDto(publisher.getUser(), publisher))
                .toList();
    }

    @Override
    public PublisherAdminDto getPublisherAdmin(int userId) {
        return publisherRepository
                .findWithUserByUserId(userId)
                .map(publisher -> publisherMapper.toAdminDto(publisher.getUser(), publisher))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản"));
    }

    @Override
    public List<UserOption> getUserOptions() {
        return userRepository.findAllByRole(UserRole.READER).stream()
                .map(userMapper::toUserOption)
                .toList();
    }

    @Transactional
    @Override
    public PublisherDto createPublisher(PublisherForm publisherForm) {
        if (publisherRepository.existsById(publisherForm.getUserId())) {
            throw new ResourceNotFoundException("Nhà xuất bản đã tồn tại");
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
