package com.harry.clio.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record BookAuthorJson(
        @NotNull(message = "Id tác giả không được để trống") Integer authorId,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String authorFullname,

        @NotNull(message = "Vai trò tác giả không được để trống")
        BookAuthorRole role) {}
