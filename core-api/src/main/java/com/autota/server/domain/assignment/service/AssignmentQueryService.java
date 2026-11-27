package com.autota.server.domain.assignment.service;

import com.autota.server.domain.assignment.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 다른 도메인이 과제 정보를 읽을 때 쓰는 조회 전용 서비스.
 * CourseService가 이 클래스를 주입받으므로 순환 의존을 막기 위해 repository만 의존한다(다른 도메인 service 주입 금지).
 */
@Service
@RequiredArgsConstructor
public class AssignmentQueryService {
    private final AssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public int countByCourse(Long courseId) {
        return (int) assignmentRepository.countByCourseId(courseId);
    }
}
