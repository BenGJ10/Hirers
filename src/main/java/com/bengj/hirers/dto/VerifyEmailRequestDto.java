package com.bengj.hirers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "\\d{6}", message = "OTP code must be exactly 6 digits")
        String otpCode
) {
}
