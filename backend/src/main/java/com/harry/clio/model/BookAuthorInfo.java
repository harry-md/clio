package com.harry.clio.model;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record BookAuthorInfo(
        @NotNull(message = "Id tác giả không được để trống") Integer authorId,

        String authorFullname,

        @NotNull(message = "Vai trò tác giả không được để trống")
        BookAuthorRole role)
        implements Serializable {}
