package com.autota.server.domain.submission.entity;

/** 채점 진행 상태. API에도 같은 값으로 나간다. */
public enum SubmissionStatus {
    PENDING, RUNNING, DONE, FAILED
}
