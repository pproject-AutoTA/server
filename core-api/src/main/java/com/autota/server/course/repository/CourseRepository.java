package com.autota.server.course.repository;

import com.autota.server.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByJoinCode(String joinCode);

    /** 참여 코드로 수업 조회. 교수 이름을 쓰므로 교수를 함께 가져온다. */
    @Query("select c from Course c join fetch c.professor where c.joinCode = :joinCode")
    Optional<Course> findByJoinCodeWithProfessor(@Param("joinCode") String joinCode);

    /** 교수의 개설 수업. 학기 내림차순, 이름 오름차순. */
    @Query("select c from Course c where c.professor.id = :professorId order by c.semester desc, c.title asc")
    List<Course> findOwned(@Param("professorId") Long professorId);
}
