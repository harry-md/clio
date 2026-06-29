package com.harry.clio.domain.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface UserMapper {
    User toEntity(CreateUserRequest request);

    UserResponse toDto(User user);
}
