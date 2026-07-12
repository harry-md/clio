package com.harry.clio.dto.author;

import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public record UpdateAuthorRequest(
        @Size(max = 255, message = "Tên tác giả vượt quá độ dài cho phép")
        String fullName,

        @Size(max = 20000, message = "Tiểu sử vượt quá độ dài cho phép")
        String biography,

        MultipartFile avatarFile,
        Boolean verified) {}
