package com.autota.server.course.repository;

import com.autota.server.course.entity.Course;
import com.autota.server.course.entity.Enrollment;
import com.autota.server.course.entity.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    /** 학생이 참여 중인 수업(교수 포함). 학기 내림차순, 이름 오름차순. */
    @Query("select c from Enrollment e join e.course c join fetch c.professor "
            + "where e.student.id = :studentId order by c.semester desc, c.title asc")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);

    @Query("select count(e) from Enrollment e where e.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    /** 여러 수업의 학생 수를 한 번에 센다. 학생이 없는 수업은 결과에 없다. */
    @Query("select new com.autota.server.course.repository.CourseStudentCount(e.course.id, count(e)) "
            + "from Enrollment e where e.course.id in :courseIds group by e.course.id")
    List<CourseStudentCount> countByCourseIds(@Param("courseIds") Collection<Long> courseIds);
}
