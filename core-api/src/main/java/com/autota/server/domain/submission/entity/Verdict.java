package com.autota.server.domain.submission.entity;

/** 판정. SKIPPED는 케이스 결과에만 쓴다(앞 케이스에서 컨테이너가 죽어 실행하지 못한 케이스). */
public enum Verdict {
    AC, WA, TLE, MLE, RE, SKIPPED
}
