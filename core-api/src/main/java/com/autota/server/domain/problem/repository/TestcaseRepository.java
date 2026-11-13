package com.autota.server.domain.problem.repository;

import com.autota.server.domain.problem.entity.Problem;
import com.autota.server.domain.problem.entity.Testcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestcaseRepository extends JpaRepository<Testcase, Long> {
    /** 공개 케이스만, 문제 안 순서대로. */
    @Query("select t from Testcase t where t.problem = :problem and t.isPublic = true order by t.orderNo")
    List<Testcase> findPublicByProblem(Problem problem);

    /** 공개+비공개 전부, 문제 안 순서대로. */
    List<Testcase> findByProblemOrderByOrderNoAsc(Problem problem);

    /** 공개+비공개 합계. */
    long countByProblem(Problem problem);
}
