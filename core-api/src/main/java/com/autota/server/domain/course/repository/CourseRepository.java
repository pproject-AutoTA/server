package com.autota.server.domain.course.repository;

import com.autota.server.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByJoinCode(String joinCode);
    List<Course> findByProfessorId(Long professorId);

    @Query("select e.course from Enrollment e where e.studentId = :studentId")
    List<Course> findCoursesByStudentId(Long studentId);

    @Query("select count(e) from Enrollment e where e.course.id = :courseId")
    long countStudents(Long courseId);

    Optional<Course> findCourseByJoinCode(String joinCode);
}
