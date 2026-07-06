package com.harry.clio.mapper;

import com.harry.clio.dto.UserCreateRequest;
import com.harry.clio.dto.UserResponse;
import com.harry.clio.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateRequest request);

    UserResponse toDto(User user);
}
