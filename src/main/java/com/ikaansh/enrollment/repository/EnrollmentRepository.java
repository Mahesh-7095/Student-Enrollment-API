package com.ikaansh.enrollment.repository;

import com.ikaansh.enrollment.entity.Enrollment;
import com.ikaansh.enrollment.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @Query(value = "select e.student from Enrollment e where e.course.id = :courseId",
           countQuery = "select count(e) from Enrollment e where e.course.id = :courseId")
    Page<Student> findStudentsByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    @Modifying
    @Query("delete from Enrollment e where e.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("delete from Enrollment e where e.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}
