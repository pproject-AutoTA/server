package com.autota.server.domain.submission.service;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.submission.dto.SubmitResponse;
import com.autota.server.domain.submission.entity.Submission;
import com.autota.server.domain.submission.repository.SubmissionRepository;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class StudentSubmissionService {
    private static final int MAX_CODE_BYTES = 65536;

    private final SubmissionRepository submissionRepository;
    //private final ProblemService problemService; (problem entity에다가 totalCount 열 추가하고 조회 메서드 생성)

    /**
     * 코드 제출. 저장하고 채점 요청을 큐에 넣은 뒤 바로 돌아온다(채점 결과는 result.queue로 온다).
     *
     * @Transactional을 붙이지 않는다. save()가 끝나면 바로 커밋되어야, 워커가 메시지를 받았을 때
     * 제출 행이 DB에 있다.
     *
     * TODO(problem·assignment·course): 세 도메인이 develop에 들어오면 아래 "나중에" 부분을 채운다.
     * 지금은 문제·수강·마감·재제출 확인이 없고 채점 요청도 보내지 않아서, 제출이 PENDING에 머문다.
     *
     * problem 도메인에 필요한 것(이름은 예시):
     *   ProblemJudgeInfo problemService.getJudgeInfo(problemId)  // 없으면 PROBLEM_NOT_FOUND
     *   record ProblemJudgeInfo(Long courseId, Instant dueAt, boolean allowResubmit,
     *                           int cpuTimeLimitMs, int memoryLimitMb, List<JudgeCase> testcases)
     *   testcases는 공개+비공개 전부, order_no 순(no, input, expectedOutput)
     */
    public SubmitResponse submit(AuthUser user, long problemId, String code) {
        if (code.getBytes(StandardCharsets.UTF_8).length > MAX_CODE_BYTES) {
            throw new BusinessException(ErrorCode.CODE_TOO_LARGE);
        }

        // 나중에 1) 문제 확인 (PROBLEM-001)
        //   ProblemJudgeInfo problem = problemService.getJudgeInfo(problemId);
        // 나중에 2) 수강 확인 (COURSE-004)
        //   courseService.checkAccess(problem.courseId(), user);
        // 나중에 3) 마감 확인 (SUBMISSION-002). dueAt과 같은 시각까지는 받는다.
        //   마감 판정과 저장되는 submittedAt이 같은 시각이어야 하므로, 이때 Submission 생성자가
        //   now를 받도록 바꾼다.
        //   Instant now = Instant.now();
        //   if (now.isAfter(problem.dueAt())) throw new BusinessException(ErrorCode.DEADLINE_PASSED);
        // 나중에 4) 재제출 확인 (SUBMISSION-003). 직전 제출이 FAILED면 재제출 금지 문제라도 받는다.
        //   SubmissionRepository에 추가:
        //     Optional<Submission> findFirstByProblemIdAndStudentIdOrderBySubmittedAtDescIdDesc(Long problemId, Long studentId);
        //   if (!problem.allowResubmit()) {
        //       submissionRepository.findFirstByProblemIdAndStudentIdOrderBySubmittedAtDescIdDesc(problemId, user.id())
        //               .filter(last -> last.getStatus() != SubmissionStatus.FAILED)
        //               .ifPresent(last -> { throw new BusinessException(ErrorCode.RESUBMIT_NOT_ALLOWED); });
        //   }

        Submission submission = submissionRepository.save(new Submission(problemId, user.id(), code));

        // 나중에 5) 채점 요청 발행. JudgePublisher를 필드로 주입한다.
        //   shortcut: 발행이 실패하면 제출이 PENDING에 남는다, 실제로 생기면 outbox나 재발행 배치로 바꾼다
        //   judgePublisher.publish(new JudgeRequest(submission.getId(), problemId, JudgeRequest.LANGUAGE, code,
        //           problem.cpuTimeLimitMs(), problem.memoryLimitMb(),
        //           problem.testcases().stream()
        //                   .map(t -> new JudgeRequest.Testcase(t.no(), t.input(), t.expectedOutput())).toList(),
        //           Instant.now()));

        return new SubmitResponse(
                submission.getId(),
                submission.getProblemId(),
                submission.getStatus().name(),
                LocalDateTime.ofInstant(submission.getSubmittedAt(), ZoneId.of("Asia/Seoul")));
    }
}
