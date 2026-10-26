package com.ikaansh.enrollment.dto;

import com.ikaansh.enrollment.entity.Student;
import java.time.LocalDate;

public record StudentResponse(Long id, String name, String email, LocalDate enrollmentDate) {
    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getEnrollmentDate());
    }
}
