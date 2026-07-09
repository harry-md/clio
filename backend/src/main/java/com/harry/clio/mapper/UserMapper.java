package com.harry.clio.mapper;

import com.harry.clio.dto.user.CreateUserRequest;
import com.harry.clio.dto.user.UserResponse;
import com.harry.clio.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "avatar", ignore = true)
    User toEntity(CreateUserRequest request);

    UserResponse toDto(User user);
}
