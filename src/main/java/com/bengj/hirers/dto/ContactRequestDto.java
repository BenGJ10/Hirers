package com.bengj.hirers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record ContactRequestDto(
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Please enter a valid email")
        String email,

        @NotBlank(message = "Message cannot be empty")
        @Size(min = 10, message = "Message must be at least 10 characters long")
        String message,

        @NotBlank(message = "Name cannot be empty")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters long")
        String name,

        @NotBlank(message = "Subject cannot be empty")
        @Size(min = 5, max = 50, message = "Subject must be between 3 and 50 characters long")
        String subject,

        @NotBlank(message = "User-Type cannot be empty")
        @Pattern(regexp = "Job Seeker|Employer|Other", message = "User-Type must be either Job Seeker, Employer, or Other")
        String userType) implements Serializable {
}