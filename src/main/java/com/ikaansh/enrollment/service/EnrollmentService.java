package com.ikaansh.enrollment.service;

import com.ikaansh.enrollment.dto.EnrollmentRequest;
import com.ikaansh.enrollment.dto.EnrollmentResponse;
import com.ikaansh.enrollment.entity.Course;
import com.ikaansh.enrollment.entity.Enrollment;
import com.ikaansh.enrollment.entity.Student;
import com.ikaansh.enrollment.exception.ConflictException;
import com.ikaansh.enrollment.exception.ResourceNotFoundException;
import com.ikaansh.enrollment.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollments;
    private final StudentService studentService;
    private final CourseService courseService;

    public EnrollmentService(EnrollmentRepository enrollments, StudentService studentService, CourseService courseService) {
        this.enrollments = enrollments;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    public EnrollmentResponse enroll(EnrollmentRequest req) {
        Student student = studentService.find(req.studentId());
        Course course = courseService.find(req.courseId());

        if (enrollments.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new ConflictException("Student " + student.getId() + " is already enrolled in course " + course.getId());
        }
        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setCourse(course);
        return EnrollmentResponse.from(enrollments.save(e));
    }

    public void unenroll(Long enrollmentId) {
        Enrollment e = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));
        enrollments.delete(e);
    }
}
