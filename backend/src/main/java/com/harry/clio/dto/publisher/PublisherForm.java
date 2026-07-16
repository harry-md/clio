package com.harry.clio.dto.publisher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PublisherForm {
    @NotNull(message = "Phải chọn user")
    Integer userId;

    @NotBlank(message = "Phải nhập số tài khoản")
    String bankAccountNumber;
}
