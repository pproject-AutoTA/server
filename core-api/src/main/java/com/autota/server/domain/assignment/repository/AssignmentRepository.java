package com.autota.server.domain.assignment.repository;

import com.autota.server.domain.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
