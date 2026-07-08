package com.harry.clio.mapper;

import com.harry.clio.dto.user.CreateUserRequest;
import com.harry.clio.dto.user.UserResponse;
import com.harry.clio.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    UserResponse toDto(User user);
}
