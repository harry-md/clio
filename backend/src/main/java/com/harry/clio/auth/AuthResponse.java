package com.harry.clio.auth;

public record AuthResponse(
        String username, String firstName, String lastName, String avatar, String role) {}
