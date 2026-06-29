package com.harry.clio.user;

import java.time.Instant;

record UserResponse(
        String username,
        String firstName,
        String lastName,
        String email,
        String avatar,
        UserRole role,
        Instant createdAt,
        Instant updatedAt) {}
