package com.harry.clio.domain.user;

import java.time.Instant;

public record UserResponse(
        String username,
        String password,
        String firstName,
        String lastName,
        String email,
        String avatar,
        UserRole role,
        Instant createdAt,
        Instant updatedAt) {}
