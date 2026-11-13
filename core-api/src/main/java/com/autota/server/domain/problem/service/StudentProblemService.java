package com.autota.server.domain.problem.service;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.problem.dto.StudentProblemResponse;
import com.autota.server.domain.problem.entity.Problem;
import com.autota.server.domain.problem.repository.ProblemRepository;
import com.autota.server.domain.problem.repository.TestcaseRepository;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentProblemService {
    private final ProblemRepository problemRepository;
    private final TestcaseRepository testcaseRepository;

    /** 공개 케이스만 내리고, 비공개는 totalTestcaseCount에만 포함한다. */
    @Transactional(readOnly = true)
    public StudentProblemResponse getProblem(Long problemId, AuthUser student) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        // TODO(assignment·course): 권한 확인이 없고 courseId·dueAt이 null이다.
        //  배경과 선행 작업(AssignmentService.getInfo 만들기 등)은 ProfessorProblemService의 클래스 주석 참고.
        //  아래 두 줄(courseId, dueAt = null)을 이렇게 바꾼다:
        //      AssignmentInfo assignment = assignmentService.getInfo(problem.getAssignmentId());
        //      courseService.checkAccess(assignment.courseId(), student); // 참여하지 않은 수업이면 COURSE-004
        //      Long courseId = assignment.courseId();
        //      LocalDateTime dueAt = assignment.dueAt();
        //  지금 동작: 참여하지 않은 수업의 문제도 조회된다.
        Long courseId = null;
        LocalDateTime dueAt = null;

        List<StudentProblemResponse.Testcase> testcases = testcaseRepository.findPublicByProblem(problem).stream()
                .map(t -> new StudentProblemResponse.Testcase(t.getOrderNo(), t.getInput(), t.getExpectedOutput()))
                .toList();

        return new StudentProblemResponse(
                problem.getId(),
                problem.getAssignmentId(),
                courseId,
                problem.getTitle(),
                problem.getDescription(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getCpuTimeLimitMs(),
                problem.getMemoryLimitMb(),
                problem.isAllowResubmit(),
                dueAt,
                testcases,
                (int) testcaseRepository.countByProblem(problem)
        );
    }
}
