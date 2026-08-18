package com.harry.clio.dto.user;

import jakarta.validation.constraints.Size;

public record UserFilterRequest(
        @Size(max = 255) String username,

        @Size(max = 255) String firstName,

        @Size(max = 255) String lastName,

        @Size(max = 255) String email) {}
