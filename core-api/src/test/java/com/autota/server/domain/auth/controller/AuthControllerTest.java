package com.autota.server.domain.auth.controller;

import com.autota.server.domain.auth.dto.LoginResponse;
import com.autota.server.domain.auth.entity.Role;
import com.autota.server.domain.auth.jwt.JwtProvider;
import com.autota.server.domain.auth.service.AuthService;
import com.autota.server.global.config.CorsConfig;
import com.autota.server.global.config.SecurityConfig;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import com.autota.server.global.security.RestAccessDeniedHandler;
import com.autota.server.global.security.RestAuthenticationEntryPoint;
import com.autota.server.global.security.SecurityErrorWriter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 로그인 API와 공통 인증·예외 처리(필터, 에러 응답, CORS)를 DB 없이 검증한다. */
@WebMvcTest({AuthController.class, AuthControllerTest.ProbeController.class})
@Import({SecurityConfig.class, CorsConfig.class, JwtProvider.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, SecurityErrorWriter.class,
        AuthControllerTest.ProbeController.class, AuthControllerTest.MethodSecurity.class})
@TestPropertySource(properties = {
        "jwt.secret=" + AuthControllerTest.SECRET,
        "jwt.expires-in-seconds=3600",
        "cors.allowed-origins=http://localhost:3000"
})
class AuthControllerTest {
    static final String SECRET = "test-secret-test-secret-test-secret-1234";
    private static final String LOGIN = "/api/v1/auth/login";

    @Autowired MockMvc mockMvc;
    @MockitoBean AuthService authService;

    private final JwtProvider jwt = new JwtProvider(SECRET, 3600);

    /** 컨트롤러 안의 인가 실패와 경로 변수 타입 오류를 확인하기 위한 테스트 전용 엔드포인트 */
    @RestController
    @RequestMapping("/api/v1")
    static class ProbeController {
        @GetMapping("/professor/probe")
        String professorPath() {
            return "ok";
        }

        @GetMapping("/admin/probe")
        String adminPath() {
            return "ok";
        }

        /** 필터는 통과(학생·교수 공통 경로)하고 컨트롤러에서 한 번 더 좁히는 경우 */
        @GetMapping("/probe/professor-only")
        @PreAuthorize("hasRole('PROFESSOR')")
        String professorOnly() {
            return "ok";
        }

        @GetMapping("/probe/{id}")
        String byId(@PathVariable Long id) {
            return "ok";
        }

        // 학생 전용 경로 (SecurityConfig에서 메서드+경로로 지정)
        @PostMapping("/enrollments")
        String enroll() {
            return "ok";
        }

        @PostMapping("/problems/{id}/submissions")
        String submit(@PathVariable Long id) {
            return "ok";
        }

        @GetMapping("/problems/{id}")
        String problem(@PathVariable Long id) {
            return "ok";
        }

        @GetMapping("/problems/{id}/submissions/me")
        String mySubmissions(@PathVariable Long id) {
            return "ok";
        }

        @GetMapping("/submissions/me/{id}")
        String mySubmission(@PathVariable Long id) {
            return "ok";
        }

        /** 학생 전용 경로와 접두어가 같지만 공통인 경로 */
        @GetMapping("/problems/{id}/common-probe")
        String problemCommon(@PathVariable Long id) {
            return "ok";
        }
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurity {
    }

    private String bearer(Long id, Role role) {
        return "Bearer " + jwt.createAccessToken(id, role);
    }

    private static final String VALID_BODY = "{\"email\":\"kim@univ.ac.kr\",\"password\":\"pw1234\"}";

    // ---- 로그인 ----

    @Test
    void 학생_로그인_성공은_200과_명세_형태로_응답한다() throws Exception {
        when(authService.login(eq("kim@univ.ac.kr"), eq("pw1234"))).thenReturn(
                new LoginResponse("token-value", 3600,
                        new LoginResponse.UserInfo(7L, "김학생", "kim@univ.ac.kr", Role.STUDENT, "20240001")));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("COMMON-200"))
                .andExpect(jsonPath("$.data.accessToken").value("token-value"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andExpect(jsonPath("$.data.user.id").value(7))
                .andExpect(jsonPath("$.data.user.name").value("김학생"))
                .andExpect(jsonPath("$.data.user.email").value("kim@univ.ac.kr"))
                .andExpect(jsonPath("$.data.user.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.user.studentNo").value("20240001"))
                // 성공 응답은 success·code·message·data 네 필드뿐이다
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$", allOf(hasKey("success"), hasKey("code"), hasKey("message"), hasKey("data"))));
    }

    @Test
    void 학생이_아니면_studentNo는_null로_내려간다() throws Exception {
        when(authService.login(eq("park@univ.ac.kr"), any())).thenReturn(
                new LoginResponse("t", 3600, new LoginResponse.UserInfo(2L, "박교수", "park@univ.ac.kr", Role.PROFESSOR, null)));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"park@univ.ac.kr\",\"password\":\"pw1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("PROFESSOR"))
                .andExpect(jsonPath("$.data.user", hasKey("studentNo")))
                .andExpect(jsonPath("$.data.user.studentNo").value(nullValue()));
    }

    @Test
    void 이메일_또는_비밀번호가_틀리면_401_AUTH_003() throws Exception {
        when(authService.login(any(), any())).thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTH-003"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다"))
                .andExpect(jsonPath("$", allOf(hasKey("data"), hasKey("errors"), hasKey("timestamp"), hasKey("path"))))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors").value(nullValue())) // 검증 에러가 아니면 errors는 null
                .andExpect(jsonPath("$.path").value(LOGIN))
                .andExpect(jsonPath("$.timestamp", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")));
    }

    @Test
    void 입력값이_틀리면_COMMON_400과_한국어_errors() throws Exception {
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors", hasSize(2)))
                .andExpect(jsonPath("$.errors[0]", allOf(hasKey("field"), hasKey("rejectedValue"), hasKey("reason"))))
                .andExpect(jsonPath("$.errors[*].field", hasItem("email")))
                .andExpect(jsonPath("$.errors[*].reason", hasItem("올바른 이메일 형식이 아닙니다.")))
                .andExpect(jsonPath("$.errors[*].reason", hasItem("비밀번호는 필수입니다")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("password")));
    }

    @Test
    void 본문이_없거나_깨진_JSON은_COMMON_400() throws Exception {
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"));
    }

    // ---- 토큰 검사 (401) ----

    @Test
    void 토큰이_없으면_401_COMMON_401() throws Exception {
        mockMvc.perform(get("/api/v1/probe/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON-401"))
                .andExpect(jsonPath("$.path").value("/api/v1/probe/1"));
    }

    @Test
    void 깨진_토큰이나_다른_키로_서명한_토큰은_401_AUTH_001() throws Exception {
        mockMvc.perform(get("/api/v1/probe/1").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"));

        String forged = new JwtProvider("another-secret-another-secret-12345678", 3600)
                .createAccessToken(1L, Role.ADMIN);
        mockMvc.perform(get("/api/v1/probe/1").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void role이_없는_토큰도_401_AUTH_001() throws Exception {
        String noRole = Jwts.builder()
                .subject("1")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/v1/probe/1").header("Authorization", "Bearer " + noRole))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void 만료된_토큰은_401_AUTH_002() throws Exception {
        String expired = new JwtProvider(SECRET, -10).createAccessToken(1L, Role.STUDENT);

        mockMvc.perform(get("/api/v1/probe/1").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
    }

    @Test
    void Accept가_JSON이_아니어도_인증_실패는_JSON으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/probe/1").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value("COMMON-401"));
    }

    // ---- 인가·공통 예외 ----

    @Test
    void 컨트롤러_안의_인가_실패는_500이_아니라_403_COMMON_403() throws Exception {
        mockMvc.perform(get("/api/v1/probe/professor-only").header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON-403"));

        mockMvc.perform(get("/api/v1/probe/professor-only").header("Authorization", bearer(2L, Role.PROFESSOR)))
                .andExpect(status().isOk());
    }

    // ---- 경로별 권한 (필터 단계) ----

    @Test
    void 공통_경로는_학생과_교수만_통과하고_관리자는_403() throws Exception {
        mockMvc.perform(get("/api/v1/probe/1").header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/probe/1").header("Authorization", bearer(2L, Role.PROFESSOR)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/probe/1").header("Authorization", bearer(9L, Role.ADMIN)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON-403"));
    }

    @Test
    void professor_경로는_교수만_통과한다() throws Exception {
        mockMvc.perform(get("/api/v1/professor/probe").header("Authorization", bearer(2L, Role.PROFESSOR)))
                .andExpect(status().isOk());
        for (Role other : new Role[]{Role.STUDENT, Role.ADMIN}) {
            mockMvc.perform(get("/api/v1/professor/probe").header("Authorization", bearer(1L, other)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("COMMON-403"));
        }
    }

    @Test
    void admin_경로는_관리자만_통과한다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/probe").header("Authorization", bearer(9L, Role.ADMIN)))
                .andExpect(status().isOk());
        for (Role other : new Role[]{Role.STUDENT, Role.PROFESSOR}) {
            mockMvc.perform(get("/api/v1/admin/probe").header("Authorization", bearer(1L, other)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("COMMON-403"));
        }
    }

    @Test
    void 학생_전용_경로는_학생만_통과하고_교수와_관리자는_403() throws Exception {
        String[][] studentOnly = {
                {"POST", "/api/v1/enrollments"},
                {"POST", "/api/v1/problems/1/submissions"},
                {"GET", "/api/v1/problems/1"},
                {"GET", "/api/v1/problems/1/submissions/me"},
                {"GET", "/api/v1/submissions/me/1"},
        };
        for (String[] api : studentOnly) {
            var student = "POST".equals(api[0]) ? post(api[1]) : get(api[1]);
            mockMvc.perform(student.header("Authorization", bearer(1L, Role.STUDENT)))
                    .andExpect(status().isOk());
            for (Role other : new Role[]{Role.PROFESSOR, Role.ADMIN}) {
                var req = "POST".equals(api[0]) ? post(api[1]) : get(api[1]);
                mockMvc.perform(req.header("Authorization", bearer(2L, other)))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("COMMON-403"));
            }
        }
    }

    @Test
    void 학생_전용과_접두어가_같아도_다른_경로는_교수도_통과한다() throws Exception {
        mockMvc.perform(get("/api/v1/problems/1/common-probe").header("Authorization", bearer(2L, Role.PROFESSOR)))
                .andExpect(status().isOk());
    }

    @Test
    void 토큰이_없으면_경로별_권한_검사보다_먼저_401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/probe"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("COMMON-401"));
        mockMvc.perform(get("/api/v1/professor/probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 경로_변수_타입이_틀리면_400_COMMON_400() throws Exception {
        mockMvc.perform(get("/api/v1/probe/abc").header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"))
                .andExpect(jsonPath("$.errors[0].field").value("id"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("abc"));
    }

    @Test
    void 없는_경로와_잘못된_메서드는_명세_표의_COMMON_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/nothing").header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"))
                .andExpect(jsonPath("$.errors").value(nullValue()));

        mockMvc.perform(get(LOGIN).header("Authorization", bearer(1L, Role.STUDENT)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON-400"));
    }

    // ---- CORS ----

    @Test
    void 허용한_오리진의_사전_요청은_통과한다() throws Exception {
        mockMvc.perform(options(LOGIN)
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void 허용하지_않은_오리진의_사전_요청은_거부한다() throws Exception {
        mockMvc.perform(options(LOGIN)
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
