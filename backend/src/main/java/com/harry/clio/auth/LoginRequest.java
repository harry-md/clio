package com.harry.clio.auth;

import jakarta.validation.constraints.NotNull;

record LoginRequest(
        @NotNull(message = "Tên đăng nhập không được để trống")
        String username,

        @NotNull(message = "Mật khẩu không được để trống") String password) {}
