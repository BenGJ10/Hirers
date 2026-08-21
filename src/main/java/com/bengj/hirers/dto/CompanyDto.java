package com.bengj.hirers.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


public record CompanyDto(
        Long id,

        @NotBlank(message = "Name can not be empty")
        String name,

        @NotBlank(message = "Logo can not be empty")
        String logo,

        @NotBlank(message = "Industry can not be empty")
        String industry,

        @NotBlank(message = "Size can not be empty")
        String size,

        @DecimalMin(value = "0.0", message = "Rating must be at least 0")
        @DecimalMax(value = "5.0", message = "Rating must be at most 5")
        BigDecimal rating,

        @NotBlank(message = "Locations can not be empty")
        String locations,

        @Min(value = 1800, message = "Founded must be 1800 or later")
        @Max(value = 2030, message = "Founded must be 2030 or earlier")
        @NotNull(message = "Founded can not be null")
        Integer founded,

        @NotBlank(message = "Description can not be empty")
        String description,

        @Min(value = 1, message = "Employees must be greater than or equal to 1")
        Integer employees,

        @NotBlank(message = "Website can not be empty")
        String website,

        Instant createdAt, List<JobDto> jobs) {
}