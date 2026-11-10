package com.autota.server.submission.entity;

/** DENIED = 금지 목록에 걸림, NOT_ALLOWED = 허용 목록 밖, MISSING_REQUIRED = 필수 요소 없음 */
public enum LintReason {
    DENIED, NOT_ALLOWED, MISSING_REQUIRED
}
