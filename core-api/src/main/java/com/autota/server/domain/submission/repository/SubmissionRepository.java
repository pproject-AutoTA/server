package com.autota.server.domain.submission.repository;

import com.autota.server.domain.submission.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /** 한 학생이 한 문제에 낸 제출 이력. 최신순이라 첫 행이 성적 대상(마지막 제출)이다. */
    @Query("select new com.autota.server.domain.submission.repository.SubmissionSummary("
            + "s.id, s.status, s.verdict, s.passedCount, s.submittedAt) "
            + "from Submission s "
            + "where s.studentId = :studentId and s.problemId = :problemId "
            + "order by s.submittedAt desc, s.id desc")
    List<SubmissionSummary> findSummaries(@Param("studentId") Long studentId,
                                          @Param("problemId") Long problemId);
}
