package com.autota.server.domain.problem.repository;

import com.autota.server.domain.problem.entity.TestcaseGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestcaseGroupRepository extends JpaRepository<TestcaseGroup, Long> {
}
