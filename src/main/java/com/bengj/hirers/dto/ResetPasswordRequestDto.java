package com.bengj.hirers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not in valid format")
        String email,

        @NotBlank(message = "Verification code is required")
        @Pattern(regexp = "^\\d{6}$", message = "Verification code must be exactly 6 digits")
        String otpCode,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 20, message = "Password length must be between 8 and 20 characters")
        String newPassword
) {}
