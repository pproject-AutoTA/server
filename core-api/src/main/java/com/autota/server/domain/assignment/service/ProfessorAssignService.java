package com.autota.server.domain.assignment.service;

import com.autota.server.domain.assignment.dto.AssignmentRequest;
import com.autota.server.domain.assignment.dto.ProfessorAssignmentResponse;
import com.autota.server.domain.assignment.entity.Assignment;
import com.autota.server.domain.assignment.repository.AssignmentRepository;
import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProfessorAssignService {
    /** 요청·응답 시각은 Asia/Seoul 기준(명세). DB에는 Instant로 저장한다. */
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final AssignmentRepository assignmentRepository;
    private final CourseService courseService;

    public ProfessorAssignmentResponse assignCourse(AssignmentRequest request, Long courseId, AuthUser professor) {
        courseService.checkAccess(courseId, professor); // 본인이 개설한 수업만

        Assignment assignment = assignmentRepository.save(
                new Assignment(
                        courseId,
                        request.title(),
                        request.dueAt().atZone(SEOUL).toInstant()
                )
        );

        ProfessorAssignmentResponse professorAssignmentResponse = new ProfessorAssignmentResponse(
                assignment.getId(),
                assignment.getCourseId(),
                assignment.getTitle(),
                LocalDateTime.ofInstant(assignment.getDueAt(), SEOUL),
                0 // 새 과제라 문제는 아직 없다
        );

        return professorAssignmentResponse;
    }
}
