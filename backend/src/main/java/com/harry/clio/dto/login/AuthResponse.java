package com.harry.clio.dto.login;

public record AuthResponse(
        String username, String firstName, String lastName, String avatar, String role) {}
