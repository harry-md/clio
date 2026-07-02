package com.harry.clio.dto;

import com.harry.clio.entity.UserRole;

import java.time.Instant;

public record UserResponse(
        String username,
        String firstName,
        String lastName,
        String email,
        String avatar,
        UserRole role,
        Instant createdAt,
        Instant updatedAt) {}
