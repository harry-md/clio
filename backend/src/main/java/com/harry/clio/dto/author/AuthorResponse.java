package com.harry.clio.dto.author;

public record AuthorResponse(
        Integer id, String fullName, String biography, String avatar, boolean verified) {}
