package com.harry.clio.entity;


import jakarta.validation.constraints.NotNull;

public record BookAuthorJson(
        @NotNull(message = "Id tác giả không được để trống") Integer authorId,

        String authorFullname,

        @NotNull(message = "Vai trò tác giả không được để trống")
        BookAuthorRole role) {}
