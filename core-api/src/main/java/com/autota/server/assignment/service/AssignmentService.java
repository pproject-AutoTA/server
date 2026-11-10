package com.autota.server.assignment.service;

import com.autota.server.assignment.repository.AssignmentRepository;
import com.autota.server.assignment.repository.CourseAssignmentCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;

    /** 수업 id별 과제 수. 과제가 없는 수업은 맵에 없으니 getOrDefault(id, 0L)로 읽는다. */
    public Map<Long, Long> countByCourseIds(Collection<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return assignmentRepository.countByCourseIds(courseIds).stream()
                .collect(Collectors.toMap(CourseAssignmentCount::courseId, CourseAssignmentCount::count));
    }
}
