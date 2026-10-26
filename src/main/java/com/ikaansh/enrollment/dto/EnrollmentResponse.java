package com.ikaansh.enrollment.dto;

import com.ikaansh.enrollment.entity.Enrollment;
import java.time.LocalDateTime;

public record EnrollmentResponse(Long id, Long studentId, String studentName, Long courseId, String courseTitle,
                                 LocalDateTime enrolledAt) {
    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(e.getId(), e.getStudent().getId(), e.getStudent().getName(),
                e.getCourse().getId(), e.getCourse().getTitle(), e.getEnrolledAt());
    }
}
