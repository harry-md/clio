package com.harry.clio.mapper;

import com.harry.clio.dto.publisher.AdminPublisherDto;
import com.harry.clio.dto.publisher.PublisherDto;
import com.harry.clio.model.Publisher;
import com.harry.clio.model.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    PublisherDto toDto(Publisher publisher);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatar", source = "user.avatar")
    @Mapping(target = "bankAccountNumber", source = "publisher.bankAccountNumber")
    @Mapping(target = "balance", source = "publisher.balance")
    AdminPublisherDto toAdminDto(User user, Publisher publisher);
}
