package com.bengj.hirers.dto;

import com.bengj.hirers.enums.JobApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateJobApplicationDto(
        @NotNull(message = "Application ID is required")
        Long applicationId,
        @NotNull(message = "Status is required")
        JobApplicationStatus status,
        String notes
) {}
