package com.harry.clio.dto.review;

public record ReviewResponse(
        Integer id, Integer userId, String username, String avatar, int rating, String comment) {}
