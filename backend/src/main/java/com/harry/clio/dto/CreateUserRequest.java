package com.harry.clio.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public record CreateUserRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 255, message = "Tên đăng nhập vượt quá độ dài cho phép")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 255, message = "Mật khẩu vượt quá độ dài cho phép")
        String password,

        @NotBlank(message = "Tên không được để trống")
        @Size(max = 255, message = "Tên vượt quá độ dài cho phép")
        String firstName,

        @NotBlank(message = "Họ không được để trống")
        @Size(max = 255, message = "Họ vượt quá độ dài cho phép")
        String lastName,

        @NotBlank @Email String email,

        @JsonIgnore MultipartFile avatar) {}
