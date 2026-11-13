package com.autota.server.domain.problem.service;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.problem.dto.ProblemCreateRequest;
import com.autota.server.domain.problem.dto.ProblemCreateResponse;
import com.autota.server.domain.problem.dto.ProfessorProblemResponse;
import com.autota.server.domain.problem.entity.Problem;
import com.autota.server.domain.problem.entity.ScoringMode;
import com.autota.server.domain.problem.entity.Testcase;
import com.autota.server.domain.problem.entity.TestcaseGroup;
import com.autota.server.domain.problem.repository.ProblemRepository;
import com.autota.server.domain.problem.repository.TestcaseGroupRepository;
import com.autota.server.domain.problem.repository.TestcaseRepository;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO(assignment·course) 이어서 만들기 안내 — problem 도메인의 미완성 3곳(이 클래스 2곳, StudentProblemService 1곳)에 공통.
 *
 * [왜 비어 있나]
 * feat/#24/problem은 develop에서 갈라졌는데, 작성 시점에 course(PR #22)와 assignment(브랜치 feat/#23/assignment)가
 * develop에 없었다. 그래서 과제 확인·수업 권한 확인·courseId·dueAt을 넣지 못했다. 그 외 로직은 명세대로 끝나 있다.
 *
 * [시작 조건]
 * develop에 domain/course, domain/assignment 폴더가 들어온 뒤 이 브랜치에 develop을 받아온다.
 *
 * [이미 있는 것] course 도메인 (feat/#23/assignment의 커밋 1b6f4c7)
 * - CourseService.checkAccess(Long courseId, AuthUser authUser): 반환값 없음.
 *   없는 수업이면 COURSE-001, 교수가 개설하지 않았거나 학생이 참여하지 않은 수업이면 COURSE-004를 던진다.
 *   교수·학생을 알아서 구분하므로 세 곳 모두 같은 호출을 쓴다.
 *
 * [먼저 만들어야 하는 것] assignment 도메인 (피호출 측이라 별도 커밋으로 먼저, scope는 assignment)
 * - AssignmentService에 과제 id로 과제 정보를 돌려주는 메서드가 아직 없다. 예:
 *     AssignmentInfo getInfo(Long assignmentId)   // 없으면 BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND)
 *     record AssignmentInfo(Long courseId, LocalDateTime dueAt)   // assignment/dto, 엔티티는 도메인 밖으로 내보내지 않는다
 * - dueAt은 엔티티에서 Instant다. LocalDateTime.ofInstant(dueAt, ZoneId.of("Asia/Seoul"))로 바꿔서 준다.
 * - AssignmentRepository를 problem에서 직접 쓰지 않는다(AGENTS.md: 다른 도메인은 service로만).
 *
 * [각 위치에서 할 일] 아래 메서드들의 TODO(assignment·course) 주석에 넣을 코드를 적어 두었다.
 *
 * [끝난 뒤 같이 볼 곳]
 * - assignment의 AssignmentService.getAssignments: TODO(problem)으로 problems[]가 빈 배열이다.
 *   과제 id 목록으로 문제(id, title, resubmitAllowed, testcaseCount)를 돌려주는 메서드를 problem 쪽 service에 만들어 채운다.
 * - 명세: private-docs/AutoTA-서버A-구현명세서-v2.md 의 5번·11번·12번(문제), 3번(과제·문제 목록).
 */
@Service
@RequiredArgsConstructor
public class ProfessorProblemService {
    private final ProblemRepository problemRepository;
    private final TestcaseGroupRepository testcaseGroupRepository;
    private final TestcaseRepository testcaseRepository;

    /** 문제와 테스트케이스를 한 번에 등록한다. 그룹은 서버가 1개(100점, RATIO)를 만들어 모든 케이스를 넣는다. */
    @Transactional
    public ProblemCreateResponse createProblem(Long assignmentId, ProblemCreateRequest request, AuthUser professor) {
        // TODO(assignment·course): 과제 확인과 권한 확인이 빠져 있다. 배경과 선행 작업은 클래스 주석 참고.
        //  넣을 코드(이 자리, 저장보다 먼저):
        //      AssignmentInfo assignment = assignmentService.getInfo(assignmentId); // 없으면 ASSIGNMENT-001
        //      courseService.checkAccess(assignment.courseId(), professor);         // 남의 수업이면 COURSE-004
        //  순서는 과제 확인 → 권한 확인. 필드에 AssignmentService, CourseService를 추가한다.
        //  지금 동작: 없는 과제 id면 FK 위반으로 500, 다른 교수의 과제에도 문제가 등록된다.

        // shortcut: 동시에 등록하면 순서가 겹칠 수 있다, 표시 순서를 바꾸는 기능(C7)이 생기면 다시 본다
        int orderNo = (int) problemRepository.countByAssignmentId(assignmentId) + 1;

        Problem problem = problemRepository.save(new Problem(
                assignmentId,
                request.title(),
                request.description(),
                request.inputFormat(),
                request.outputFormat(),
                request.timeLimitMs(),
                request.memoryLimitMb(),
                request.resubmitAllowed(),
                orderNo
        ));

        TestcaseGroup group = testcaseGroupRepository.save(
                new TestcaseGroup(problem, null, new BigDecimal("100"), ScoringMode.RATIO, 1)
        );

        List<Testcase> testcases = new ArrayList<>();
        for (int i = 0; i < request.testcases().size(); i++) {
            ProblemCreateRequest.Testcase t = request.testcases().get(i);
            testcases.add(new Testcase(problem, group, t.input(), t.expectedOutput(), t.isPublic(), i + 1));
        }
        testcaseRepository.saveAll(testcases);

        return new ProblemCreateResponse(problem.getId(), problem.getAssignmentId(), problem.getTitle(), testcases.size());
    }

    /** 비공개 케이스까지 전부 내린다. */
    @Transactional(readOnly = true)
    public ProfessorProblemResponse getProblem(Long problemId, AuthUser professor) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROBLEM_NOT_FOUND));

        // TODO(assignment·course): 권한 확인이 없고 courseId·dueAt이 null이다. 배경과 선행 작업은 클래스 주석 참고.
        //  아래 두 줄(courseId, dueAt = null)을 이렇게 바꾼다:
        //      AssignmentInfo assignment = assignmentService.getInfo(problem.getAssignmentId());
        //      courseService.checkAccess(assignment.courseId(), professor); // 남의 수업이면 COURSE-004
        //      Long courseId = assignment.courseId();
        //      LocalDateTime dueAt = assignment.dueAt();
        //  지금 동작: 다른 교수의 문제도 비공개 케이스까지 조회된다.
        Long courseId = null;
        LocalDateTime dueAt = null;

        List<ProfessorProblemResponse.Testcase> testcases = testcaseRepository.findByProblemOrderByOrderNoAsc(problem).stream()
                .map(t -> new ProfessorProblemResponse.Testcase(t.getOrderNo(), t.getInput(), t.getExpectedOutput(), t.isPublic()))
                .toList();

        return new ProfessorProblemResponse(
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
                testcases
        );
    }
}
