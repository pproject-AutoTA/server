package com.autota.server.course.service;

import com.autota.server.assignment.service.AssignmentService;
import com.autota.server.auth.entity.User;
import com.autota.server.auth.repository.UserRepository;
import com.autota.server.course.dto.EnrolledCourseResponse;
import com.autota.server.course.dto.JoinCourseResponse;
import com.autota.server.course.dto.OwnedCourseResponse;
import com.autota.server.course.entity.Course;
import com.autota.server.course.entity.Enrollment;
import com.autota.server.course.entity.EnrollmentId;
import com.autota.server.course.repository.CourseRepository;
import com.autota.server.course.repository.CourseStudentCount;
import com.autota.server.course.repository.EnrollmentRepository;
import com.autota.server.global.exception.BusinessException;
import com.autota.server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentService assignmentService;
    private final UserRepository userRepository;

    /** 수업 개설. 참여 코드는 서버가 만든다. */
    @Transactional
    public Course createCourse(Long professorId, String title, String semester) {
        User professor = userRepository.findById(professorId).orElseThrow();

        Course course = Course.create(professor, title, semester);
        while (courseRepository.existsByJoinCode(course.getJoinCode())) {
            course = Course.create(professor, title, semester); // 중복이면 다시 생성
        }
        return courseRepository.save(course);
    }

    /** 참여 코드로 수업에 참여한다. 참여 코드는 대소문자를 구분하지 않는다. */
    @Transactional
    public JoinCourseResponse joinCourse(Long studentId, String rawJoinCode) {
        String joinCode = rawJoinCode.trim().toUpperCase(Locale.ROOT);

        Course course = courseRepository.findByJoinCodeWithProfessor(joinCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (enrollmentRepository.existsById(EnrollmentId.of(course.getId(), studentId))) {
            throw new BusinessException(ErrorCode.ALREADY_ENROLLED);
        }

        User student = userRepository.findById(studentId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.save(Enrollment.join(course, student));

        Instant joinedAt = enrollment.getJoinedAt() != null ? enrollment.getJoinedAt() : Instant.now();
        return JoinCourseResponse.of(course, course.getProfessor().getUsername(), joinedAt);
    }

    /** 학생이 참여 중인 수업 목록. */
    @Transactional(readOnly = true)
    public List<EnrolledCourseResponse> getEnrolledCourses(Long studentId) {
        List<Course> courses = enrollmentRepository.findCoursesByStudentId(studentId);
        Counts counts = countsOf(courses);
        return courses.stream()
                .map(c -> EnrolledCourseResponse.of(
                        c, c.getProfessor().getUsername(), counts.students(c), counts.assignments(c)))
                .toList();
    }

    /** 교수가 개설한 수업 목록. */
    @Transactional(readOnly = true)
    public List<OwnedCourseResponse> getOwnedCourses(Long professorId) {
        List<Course> courses = courseRepository.findOwned(professorId);
        Counts counts = countsOf(courses);
        return courses.stream()
                .map(c -> OwnedCourseResponse.of(c, counts.students(c), counts.assignments(c)))
                .toList();
    }

    private Counts countsOf(List<Course> courses) {
        List<Long> ids = courses.stream().map(Course::getId).toList();
        if (ids.isEmpty()) {
            return new Counts(Map.of(), Map.of());
        }
        Map<Long, Long> students = enrollmentRepository.countByCourseIds(ids).stream()
                .collect(Collectors.toMap(CourseStudentCount::courseId, CourseStudentCount::count));
        return new Counts(students, assignmentService.countByCourseIds(ids));
    }

    /** 수업별 학생 수·과제 수. 집계에 없는 수업은 0으로 읽는다. */
    private record Counts(Map<Long, Long> students, Map<Long, Long> assignments) {
        long students(Course c) { return students.getOrDefault(c.getId(), 0L); }
        long assignments(Course c) { return assignments.getOrDefault(c.getId(), 0L); }
    }
}
