package com.autota.server.submission.entity;

/** 채점 진행 상태. 대기 → 채점중 → 완료 / 실패(시스템 오류) */
public enum SubmissionStatus {
    PENDING, RUNNING, DONE, FAILED
}
