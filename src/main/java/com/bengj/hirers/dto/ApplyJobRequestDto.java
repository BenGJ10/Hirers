package com.bengj.hirers.dto;

import jakarta.validation.constraints.NotNull;

public record ApplyJobRequestDto(
        @NotNull(message = "Job ID is required")
        Long jobId,

        String coverLetter
) {}
