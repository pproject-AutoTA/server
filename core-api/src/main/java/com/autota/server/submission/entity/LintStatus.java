package com.autota.server.submission.entity;

/** 허용 요소 검사 결과. 판정과 별개 (AC이면서 FLAGGED 가능) */
public enum LintStatus {
    PENDING, CLEAN, FLAGGED, ERROR
}
