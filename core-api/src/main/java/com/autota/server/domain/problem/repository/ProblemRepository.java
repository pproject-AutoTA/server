package com.autota.server.domain.problem.repository;

import com.autota.server.domain.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    long countByAssignmentId(Long assignmentId);
}
