package com.bengj.hirers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not in valid format")
        String email
) {}
