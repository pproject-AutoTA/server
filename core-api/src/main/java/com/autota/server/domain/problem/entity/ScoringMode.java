package com.autota.server.domain.problem.entity;

public enum ScoringMode {
    /** 통과 비율만큼 배점을 준다. */
    RATIO,
    /** 그룹의 케이스를 모두 통과해야 배점을 준다. */
    ALL_OR_NOTHING
}
