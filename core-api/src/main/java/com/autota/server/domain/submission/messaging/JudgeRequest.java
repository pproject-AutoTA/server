package com.autota.server.domain.submission.messaging;

import java.time.Instant;
import java.util.List;

/** judge.queue로 보내는 채점 요청. 필드는 contracts/judge-request.schema.json과 같아야 한다. */
public record JudgeRequest(
        Long submissionId,
        Long problemId,
        String language,
        String code,
        int cpuTimeLimitMs,
        int memoryLimitMb,
        List<Testcase> testcases,
        Instant requestedAt // 큐 메시지는 UTC(ISO-8601)
) {
    public static final String LANGUAGE = "python3.12";

    public record Testcase(int no, String input, String expectedOutput) {
    }
}
