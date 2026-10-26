package com.ikaansh.enrollment.service;

import com.ikaansh.enrollment.dto.*;
import com.ikaansh.enrollment.entity.Student;
import com.ikaansh.enrollment.exception.ConflictException;
import com.ikaansh.enrollment.exception.ResourceNotFoundException;
import com.ikaansh.enrollment.repository.EnrollmentRepository;
import com.ikaansh.enrollment.repository.StudentRepository;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository students;
    private final EnrollmentRepository enrollments;

    public StudentService(StudentRepository students, EnrollmentRepository enrollments) {
        this.students = students;
        this.enrollments = enrollments;
    }

    public StudentResponse create(StudentRequest req) {
        if (students.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("A student with email " + req.email() + " already exists");
        }
        Student s = new Student();
        apply(s, req);
        return StudentResponse.from(students.save(s));
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> list(Pageable pageable) {
        return PageResponse.of(students.findAll(pageable), StudentResponse::from);
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long id) {
        return StudentResponse.from(find(id));
    }

    public StudentResponse update(Long id, StudentRequest req) {
        Student s = find(id);
        if (students.existsByEmailIgnoreCaseAndIdNot(req.email(), id)) {
            throw new ConflictException("A student with email " + req.email() + " already exists");
        }
        apply(s, req);
        return StudentResponse.from(students.save(s));
    }

    public void delete(Long id) {
        Student s = find(id);
        enrollments.deleteByStudentId(id); // remove dependent rows first (FK)
        students.delete(s);
    }

    Student find(Long id) {
        return students.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    private void apply(Student s, StudentRequest req) {
        s.setName(req.name().trim());
        s.setEmail(req.email().trim());
        // Keep the existing date on update if none is supplied; default to today on create.
        if (req.enrollmentDate() != null) {
            s.setEnrollmentDate(req.enrollmentDate());
        } else if (s.getEnrollmentDate() == null) {
            s.setEnrollmentDate(LocalDate.now());
        }
    }
}
