package com.ikaansh.enrollment.service;

import com.ikaansh.enrollment.dto.*;
import com.ikaansh.enrollment.entity.Course;
import com.ikaansh.enrollment.exception.ResourceNotFoundException;
import com.ikaansh.enrollment.repository.CourseRepository;
import com.ikaansh.enrollment.repository.EnrollmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;

    public CourseService(CourseRepository courses, EnrollmentRepository enrollments) {
        this.courses = courses;
        this.enrollments = enrollments;
    }

    public CourseResponse create(CourseRequest req) {
        Course c = new Course();
        apply(c, req);
        return CourseResponse.from(courses.save(c));
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> list(Pageable pageable) {
        return PageResponse.of(courses.findAll(pageable), CourseResponse::from);
    }

    @Transactional(readOnly = true)
    public CourseResponse get(Long id) {
        return CourseResponse.from(find(id));
    }

    public CourseResponse update(Long id, CourseRequest req) {
        Course c = find(id);
        apply(c, req);
        return CourseResponse.from(courses.save(c));
    }

    public void delete(Long id) {
        Course c = find(id);
        enrollments.deleteByCourseId(id);
        courses.delete(c);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> studentsInCourse(Long courseId, Pageable pageable) {
        find(courseId); // 404 if the course does not exist
        return PageResponse.of(enrollments.findStudentsByCourseId(courseId, pageable), StudentResponse::from);
    }

    Course find(Long id) {
        return courses.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    private void apply(Course c, CourseRequest req) {
        c.setTitle(req.title().trim());
        c.setDescription(req.description());
        c.setCredits(req.credits());
    }
}
