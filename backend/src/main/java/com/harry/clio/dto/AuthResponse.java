package com.harry.clio.dto;

public record AuthResponse(
        String username, String firstName, String lastName, String avatar, String role) {}
