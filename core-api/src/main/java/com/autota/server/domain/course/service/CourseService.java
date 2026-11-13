package com.autota.server.domain.course.service;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.course.dto.CourseResponse;
import com.autota.server.domain.course.entity.Course;
import com.autota.server.domain.course.repository.CourseRepository;
import com.autota.server.domain.course.repository.EnrollmentRepository;
import com.autota.server.domain.user.entity.Role;
import com.autota.server.domain.user.service.UserService;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    /**
     * 수업이 있고 로그인 사용자가 그 수업 사람인지 확인한다. 다른 도메인(과제·문제·제출)에서도 쓴다.
     * 없는 수업은 COURSE-001, 교수가 개설하지 않았거나 학생이 참여하지 않은 수업은 COURSE-004.
     */
    @Transactional(readOnly = true)
    public void checkAccess(Long courseId, AuthUser authUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        boolean allowed = authUser.is(Role.PROFESSOR)
                ? course.getProfessorId().equals(authUser.id())
                : enrollmentRepository.existsByCourseAndStudentId(course, authUser.id());
        if (!allowed) {
            throw new BusinessException(ErrorCode.COURSE_FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public int countStudents(Long courseId) {
        return (int) courseRepository.countStudents(courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getMyCourse(AuthUser authUser) {
        // 관리자는 SecurityConfig에서 막혀 여기까지 오지 않는다. 역할은 토큰에 들어 있어 DB 조회가 필요 없다.
        List<Course> courses = authUser.is(Role.STUDENT)
                ? courseRepository.findCoursesByStudentId(authUser.id())
                : courseRepository.findByProfessorId(authUser.id());

        // shortcut: 수업마다 쿼리 2번(N+1), 수업 수가 많아지면 in/group by 조회로 바꾼다
        boolean isProfessor = authUser.is(Role.PROFESSOR);
        return courses.stream()
                .sorted(Comparator.comparing(Course::getSemester).reversed()) // 최근 학기 순(YYYY-N은 문자열 비교로 충분)
                .map(course -> new CourseResponse(
                        course.getId(),
                        course.getName(),
                        course.getSemester(),
                        userService.findNameById(course.getProfessorId()).orElse(null),
                        isProfessor ? course.getJoinCode() : null, // 참여 코드는 교수에게만 내린다
                        (int) courseRepository.countStudents(course.getId()),
                        0 // assignment 도메인이 생기면 그쪽 service로 채운다
                ))
                .toList();
    }
}
