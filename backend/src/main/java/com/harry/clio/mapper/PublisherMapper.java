package com.harry.clio.mapper;

import com.harry.clio.dto.publisher.PublisherAdminDto;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.entity.Publisher;
import com.harry.clio.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    PublisherDto toDto(Publisher entity);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatar", source = "user.avatar")
    @Mapping(target = "bankAccountNumber", source = "publisher.bankAccountNumber")
    @Mapping(target = "balance", source = "publisher.balance")
    PublisherAdminDto toAdminDto(User user, Publisher publisher);
}
