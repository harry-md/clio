package com.harry.clio.service;

import com.harry.clio.dto.publisher.AdminPublisherDto;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.dto.publisher.PublisherForm;
import com.harry.clio.dto.user.UserOption;

import java.time.LocalDate;
import java.util.List;

public interface PublisherService {
    PublisherDto getPublisherByUserId(int userId);

    PublisherDto createPublisher(PublisherForm publisherForm);

    PublisherDto updatePublisher(int userId, PublisherForm publisherForm);

    PublisherDto updatePublisher(int userId, PublisherDto dto);

    List<AdminPublisherDto> getAllPublishers();

    AdminPublisherDto getPublisherAdmin(int userId);

    List<UserOption> getUserOptions();

    void calculateBookRevenueToday(LocalDate today);
}
