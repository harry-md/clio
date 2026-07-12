package com.harry.clio.mapper;

import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.entity.Publisher;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    PublisherDto toDto(Publisher entity);
}
