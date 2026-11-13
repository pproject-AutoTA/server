package com.autota.server.domain.assignment.service;

import com.autota.server.domain.assignment.dto.AssignmentResponse;
import com.autota.server.domain.assignment.repository.AssignmentRepository;
import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.course.service.CourseService;
import com.autota.server.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final AssignmentRepository assignmentRepository;
    private final CourseService courseService;

    /** 학생은 참여한 수업, 교수는 개설한 수업의 과제를 마감순으로 본다. */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignments(Long courseId, AuthUser user) {
        courseService.checkAccess(courseId, user);

        // 수강생 수는 교수에게만 내린다(학생은 null). 한 수업의 과제는 값이 모두 같다.
        Integer studentCount = user.is(Role.PROFESSOR) ? courseService.countStudents(courseId) : null;

        return assignmentRepository.findByCourseIdOrderByDueAtAsc(courseId).stream()
                .map(assignment -> new AssignmentResponse(
                        assignment.getId(),
                        assignment.getTitle(),
                        LocalDateTime.ofInstant(assignment.getDueAt(), SEOUL),
                        studentCount,
                        // TODO(problem·submission): 두 도메인이 아직 없어 문제 목록을 항상 빈 배열로 내린다.
                        //  - problem이 생기면: ProblemService로 과제별 문제(id, title, resubmitAllowed, testcaseCount)를 채운다.
                        //  - submission이 생기면: 교수는 submittedStudentCount, 학생은 mySubmission(최신 제출 1건)을 채운다.
                        //    반대 역할에게는 그 필드를 null로 내린다.
                        //  - 과제마다 조회하면 N+1이 되므로 과제 id 목록으로 한 번에 가져온다.
                        List.of()
                ))
                .toList();
    }
}
