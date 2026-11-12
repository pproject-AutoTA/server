package com.autota.server.domain.course.service;

import com.autota.server.domain.auth.jwt.AuthUser;
import com.autota.server.domain.course.dto.StudentCourseRequest;
import com.autota.server.domain.course.dto.StudentCourseResponse;
import com.autota.server.domain.course.entity.Course;
import com.autota.server.domain.course.entity.Enrollment;
import com.autota.server.domain.course.repository.CourseRepository;
import com.autota.server.domain.course.repository.EnrollmentRepository;
import com.autota.server.domain.user.service.UserService;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StudentCourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public StudentCourseResponse enrollCourse(AuthUser user, StudentCourseRequest request) {
        // 학생만 들어오는 것은 SecurityConfig가 보장한다. 발급 코드는 전부 대문자라 대문자로 맞춰 찾는다(대소문자 구분 없음).
        Course course = courseRepository.findCourseByJoinCode(request.joinCode().trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new BusinessException(ErrorCode.JOIN_CODE_NOT_FOUND));
        if (enrollmentRepository.existsByCourseAndStudentId(course, user.id())) {
            throw new BusinessException(ErrorCode.ALREADY_ENROLLED);
        }
        Enrollment enrollment = enrollmentRepository.save(
                new Enrollment(course, user.id())
        );
        String professorName = userService.findNameById(course.getProfessorId()).orElse(null);
        return new StudentCourseResponse(
                course.getId(),
                course.getName(),
                course.getSemester(),
                professorName,
                LocalDateTime.ofInstant(enrollment.getCreatedAt(), ZoneId.of("Asia/Seoul"))
        );
    }
}
