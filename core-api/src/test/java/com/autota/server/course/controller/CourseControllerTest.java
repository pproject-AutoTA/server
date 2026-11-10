package com.autota.server.course.controller;

import com.autota.server.auth.config.SecurityConfig;
import com.autota.server.auth.entity.Role;
import com.autota.server.auth.entity.User;
import com.autota.server.auth.jwt.JwtProvider;
import com.autota.server.course.dto.EnrolledCourseResponse;
import com.autota.server.course.dto.JoinCourseResponse;
import com.autota.server.course.dto.OwnedCourseResponse;
import com.autota.server.course.entity.Course;
import com.autota.server.course.service.CourseService;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import com.autota.server.global.security.RestAccessDeniedHandler;
import com.autota.server.global.security.RestAuthenticationEntryPoint;
import com.autota.server.global.security.SecurityErrorWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 컨트롤러·시큐리티·공통 응답만 검증한다. DB 없이 돌고, 서비스는 목으로 대체한다. */
@WebMvcTest(CourseController.class)
@Import({SecurityConfig.class, JwtProvider.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, SecurityErrorWriter.class})
class CourseControllerTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    @Autowired MockMvc mockMvc;
    @MockitoBean CourseService courseService;

    private final JwtProvider jwt = new JwtProvider(SECRET, 3600);

    static {
        // @WebMvcTest가 만드는 JwtProvider가 읽을 설정
        System.setProperty("jwt.secret", SECRET);
        System.setProperty("jwt.expires-in-seconds", "3600");
    }

    private String bearer(Long userId, Role role) {
        return "Bearer " + jwt.createAccessToken(userId, role);
    }

    // ---------- 인증 · 인가 ----------

    @Test
    void 토큰이_없으면_COMMON_401() throws Exception {
        mockMvc.perform(get("/api/v1/courses/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON-401"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다"))
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value((Object) null))
                .andExpect(jsonPath("$.path").value("/api/v1/courses/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 잘못된_토큰이면_AUTH_001() throws Exception {
        mockMvc.perform(get("/api/v1/courses/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void 다른_키로_서명된_토큰이면_AUTH_001() throws Exception {
        String forged = new JwtProvider("another-secret-another-secret-12345678", 3600)
                .createAccessToken(1L, Role.STUDENT);
        mockMvc.perform(get("/api/v1/courses/me").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void 만료된_토큰이면_AUTH_002() throws Exception {
        String expired = new JwtProvider(SECRET, -10).createAccessToken(1L, Role.STUDENT);
        mockMvc.perform(get("/api/v1/courses/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"))
                .andExpect(jsonPath("$.message").value("만료된 토큰입니다"));
    }

    @Test
    void 교수_토큰으로_학생_API를_호출하면_COMMON_403() throws Exception {
        mockMvc.perform(get("/api/v1/courses/me").header("Authorization", bearer(2L, Role.PROFESSOR)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON-403"))
                .andExpect(jsonPath("$.message").value("권한이 없습니다"));
    }

    @Test
    void 학생_토큰으로_교수_API를_호출하면_COMMON_403() throws Exception {
        mockMvc.perform(get("/api/v1/professor/courses").header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON-403"));
    }

    // ---------- 수업 참여 (COURSE-02) ----------

    @Test
    void 수업_참여_성공은_201과_명세_형태로_응답한다() throws Exception {
        when(courseService.joinCourse(eq(1L), eq("DS-7K92-QX"))).thenReturn(
                new JoinCourseResponse(3L, "자료구조", "2026-2", "박서연", LocalDateTime.of(2026, 11, 7, 10, 5)));

        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", bearer(1L, Role.STUDENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"joinCode\":\"DS-7K92-QX\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-201"))
                .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                .andExpect(jsonPath("$.data.courseId").value(3))
                .andExpect(jsonPath("$.data.name").value("자료구조"))
                .andExpect(jsonPath("$.data.semester").value("2026-2"))
                .andExpect(jsonPath("$.data.professorName").value("박서연"))
                .andExpect(jsonPath("$.data.joinedAt").value("2026-11-07T10:05:00"))
                .andExpect(jsonPath("$.timestamp").doesNotExist())
                .andExpect(jsonPath("$.path").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void 수업_참여는_토큰의_사용자_id로_서비스를_호출한다() throws Exception {
        when(courseService.joinCourse(any(), any())).thenReturn(
                new JoinCourseResponse(3L, "자료구조", "2026-2", "박서연", LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", bearer(42L, Role.STUDENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"joinCode\":\"DS-7K92-QX\"}"))
                .andExpect(status().isCreated());

        verify(courseService).joinCourse(42L, "DS-7K92-QX");
    }

    @Test
    void 참여_코드가_없으면_COMMON_400과_errors를_내려준다() throws Exception {
        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", bearer(1L, Role.STUDENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다"))
                .andExpect(jsonPath("$.errors[0].field").value("joinCode"))
                .andExpect(jsonPath("$.errors[0].reason").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/enrollments"));
    }

    @Test
    void 본문이_없으면_COMMON_400() throws Exception {
        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", bearer(1L, Role.STUDENT))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void 없는_참여_코드면_404_COURSE_002() throws Exception {
        when(courseService.joinCourse(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", bearer(1L, Role.STUDENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"joinCode\":\"ZZ-0000-ZZ\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COURSE-002"))
                .andExpect(jsonPath("$.message").value("참여 코드에 해당하는 수업이 없습니다"));
    }

    @Test
    void 이미_참여한_수업이면_409_COURSE_003() throws Exception {
        when(courseService.joinCourse(any(), any()))
                .thenThrow(new BusinessException(ErrorCode.ALREADY_ENROLLED));

        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", bearer(1L, Role.STUDENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"joinCode\":\"DS-7K92-QX\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COURSE-003"))
                .andExpect(jsonPath("$.message").value("이미 참여한 수업입니다"));
    }

    @Test
    void 예상하지_못한_예외는_500_COMMON_500() throws Exception {
        when(courseService.joinCourse(any(), any())).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", bearer(1L, Role.STUDENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"joinCode\":\"DS-7K92-QX\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("COMMON-500"))
                .andExpect(jsonPath("$.message").value("서버 오류입니다"));
    }

    // ---------- 내 수업 목록 (COURSE-03, 학생) ----------

    @Test
    void 내_수업_목록은_명세_필드로_응답한다() throws Exception {
        when(courseService.getEnrolledCourses(1L)).thenReturn(
                List.of(new EnrolledCourseResponse(3L, "자료구조", "2026-2", "박서연", 60, 4)));

        mockMvc.perform(get("/api/v1/courses/me").header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON-200"))
                .andExpect(jsonPath("$.data[0].id").value(3))
                .andExpect(jsonPath("$.data[0].name").value("자료구조"))
                .andExpect(jsonPath("$.data[0].semester").value("2026-2"))
                .andExpect(jsonPath("$.data[0].professorName").value("박서연"))
                .andExpect(jsonPath("$.data[0].studentCount").value(60))
                .andExpect(jsonPath("$.data[0].assignmentCount").value(4))
                .andExpect(jsonPath("$.data[0].joinCode").doesNotExist());
    }

    @Test
    void 참여한_수업이_없으면_빈_배열() throws Exception {
        when(courseService.getEnrolledCourses(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/courses/me").header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ---------- 개설 수업 목록 (COURSE-03, 교수) ----------

    @Test
    void 개설_수업_목록은_참여_코드를_포함한다() throws Exception {
        when(courseService.getOwnedCourses(2L)).thenReturn(
                List.of(new OwnedCourseResponse(3L, "자료구조", "2026-2", "DS-7K92-QX", 60, 4)));

        mockMvc.perform(get("/api/v1/professor/courses").header("Authorization", bearer(2L, Role.PROFESSOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].joinCode").value("DS-7K92-QX"))
                .andExpect(jsonPath("$.data[0].studentCount").value(60))
                .andExpect(jsonPath("$.data[0].assignmentCount").value(4))
                .andExpect(jsonPath("$.data[0].professorName").doesNotExist());
    }

    // ---------- 수업 개설 (COURSE-01) ----------

    @Test
    void 수업_개설_성공은_201과_명세_형태로_응답한다() throws Exception {
        when(courseService.createCourse(2L, "자료구조", "2026-2")).thenReturn(course());

        mockMvc.perform(post("/api/v1/professor/courses")
                        .header("Authorization", bearer(2L, Role.PROFESSOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"자료구조\",\"semester\":\"2026-2\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON-201"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.name").value("자료구조"))
                .andExpect(jsonPath("$.data.semester").value("2026-2"))
                .andExpect(jsonPath("$.data.joinCode").value("DS-7K92-QX"))
                .andExpect(jsonPath("$.data.studentCount").value(0))
                .andExpect(jsonPath("$.data.createdAt").value("2026-11-07T10:00:00"));
    }

    @Test
    void 학기_형식이_틀리면_COMMON_400() throws Exception {
        mockMvc.perform(post("/api/v1/professor/courses")
                        .header("Authorization", bearer(2L, Role.PROFESSOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"자료구조\",\"semester\":\"2026년 2학기\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"))
                .andExpect(jsonPath("$.errors[0].field").value("semester"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("2026년 2학기"));
    }

    @Test
    void 수업명이_50자를_넘으면_COMMON_400() throws Exception {
        String longName = "가".repeat(51);
        mockMvc.perform(post("/api/v1/professor/courses")
                        .header("Authorization", bearer(2L, Role.PROFESSOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + longName + "\",\"semester\":\"2026-2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void 수업명과_학기가_모두_없으면_errors가_둘() throws Exception {
        mockMvc.perform(post("/api/v1/professor/courses")
                        .header("Authorization", bearer(2L, Role.PROFESSOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    /** 응답 변환에 필요한 id·참여 코드·생성 시각을 채운 수업 */
    private static Course course() {
        User professor = new User();
        ReflectionTestUtils.setField(professor, "id", 2L);
        ReflectionTestUtils.setField(professor, "role", Role.PROFESSOR);
        ReflectionTestUtils.setField(professor, "username", "prof");

        Course course = Course.create(professor, "자료구조", "DS-7K92-QX", "2026-2");
        ReflectionTestUtils.setField(course, "id", 3L);
        // createdAt은 Asia/Seoul 기준 2026-11-07T10:00:00
        ReflectionTestUtils.setField(course, "createdAt", Instant.parse("2026-11-07T01:00:00Z"));
        return course;
    }
}
