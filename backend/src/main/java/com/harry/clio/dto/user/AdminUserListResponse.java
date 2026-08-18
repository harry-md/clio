package com.harry.clio.dto.user;

import com.harry.clio.model.UserRole;

import java.time.Instant;

public record AdminUserListResponse(
        Integer id,
        String username,
        String firstName,
        String lastName,
        String email,
        String avatar,
        UserRole role,
        Instant createdAt) {}
