package com.ikaansh.enrollment.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StudentRequest(
        @NotBlank(message = "name is required") @Size(max = 100, message = "name must be at most 100 characters") String name,
        @NotBlank(message = "email is required") @Email(message = "email must be a valid email address")
        @Size(max = 150, message = "email must be at most 150 characters") String email,
        @PastOrPresent(message = "enrollmentDate cannot be in the future") LocalDate enrollmentDate) {
}
