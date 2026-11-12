package com.autota.server.domain.course.repository;

import com.autota.server.domain.course.entity.Course;
import com.autota.server.domain.course.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByCourseAndStudentId(Course course, Long studentId);
}
