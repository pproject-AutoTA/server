package com.autota.server.assignment.repository;

import com.autota.server.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** 여러 수업의 과제 수를 한 번에 센다. 과제가 없는 수업은 결과에 없다. */
    @Query("select new com.autota.server.assignment.repository.CourseAssignmentCount(a.course.id, count(a)) "
            + "from Assignment a where a.course.id in :courseIds group by a.course.id")
    List<CourseAssignmentCount> countByCourseIds(@Param("courseIds") Collection<Long> courseIds);
}
