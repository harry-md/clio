package com.harry.clio.service;

import com.harry.clio.dto.publisher.PublisherAdminDto;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.dto.publisher.PublisherForm;
import com.harry.clio.dto.user.UserOption;

import java.util.List;

public interface PublisherService {
    PublisherDto getPublisherByUserId(int userId);

    PublisherDto createPublisher(PublisherForm publisherForm);

    PublisherDto updatePublisher(int userId, PublisherForm publisherForm);

    PublisherDto updatePublisher(int userId, PublisherDto dto);

    List<PublisherAdminDto> getAllPublishers();

    PublisherAdminDto getPublisherAdmin(int userId);

    List<UserOption> getUserOptions();
}
