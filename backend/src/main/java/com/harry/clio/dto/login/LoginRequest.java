package com.harry.clio.dto.login;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "Tên đăng nhập không được để trống")
        String username,

        @NotNull(message = "Mật khẩu không được để trống") String password) {}
