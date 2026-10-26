package com.ikaansh.enrollment.dto;

import jakarta.validation.constraints.*;

public record CourseRequest(
        @NotBlank(message = "title is required") @Size(max = 150, message = "title must be at most 150 characters") String title,
        @Size(max = 1000, message = "description must be at most 1000 characters") String description,
        @NotNull(message = "credits is required") @Min(value = 1, message = "credits must be at least 1")
        @Max(value = 10, message = "credits must be at most 10") Integer credits) {
}
