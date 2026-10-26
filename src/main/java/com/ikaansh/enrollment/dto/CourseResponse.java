package com.ikaansh.enrollment.dto;

import com.ikaansh.enrollment.entity.Course;

public record CourseResponse(Long id, String title, String description, int credits) {
    public static CourseResponse from(Course c) {
        return new CourseResponse(c.getId(), c.getTitle(), c.getDescription(), c.getCredits());
    }
}
