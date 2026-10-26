package com.ikaansh.enrollment.repository;

import com.ikaansh.enrollment.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
