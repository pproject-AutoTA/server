package com.autota.server.domain.course.service;

import com.autota.server.domain.course.dto.ProfessorCourseRequest;
import com.autota.server.domain.course.dto.ProfessorCourseResponse;
import com.autota.server.domain.course.entity.Course;
import com.autota.server.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProfessorCourseService {
    /** 0/O, 1/I 처럼 헷갈리는 글자는 뺀다. */
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CourseRepository courseRepository;

    @Transactional
    public ProfessorCourseResponse createCourse(Long professorId, ProfessorCourseRequest request) {
        Course course = courseRepository.save(
                new Course(professorId, request.name(), request.semester(), newJoinCode())
        );

        return new ProfessorCourseResponse(
                course.getId(),
                course.getName(),
                course.getSemester(),
                course.getJoinCode(),
                0,
                LocalDateTime.ofInstant(course.getCreatedAt(), ZoneId.of("Asia/Seoul"))
        );
    }

    /** ERD 예시(DS-7K92-QX)와 같은 모양의 코드를 만든다. */
    private String newJoinCode() {
        String code;
        do {
            char[] chars = "XX-XXXX-XX".toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == 'X') chars[i] = CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length()));
            }
            code = new String(chars);
        } while (courseRepository.existsByJoinCode(code));
        return code;
    }
}
