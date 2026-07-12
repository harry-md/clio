package com.harry.clio.service;

import com.harry.clio.dto.publisher.PublisherDto;

import java.util.List;

public interface PublisherService {
    PublisherDto getPublisherByUserId(int userId);

    List<PublisherDto> getAllPublishers();

    PublisherDto createPublisher(PublisherDto publisherDto);

    PublisherDto updatePublisher(int userId, PublisherDto publisherDto);
}
