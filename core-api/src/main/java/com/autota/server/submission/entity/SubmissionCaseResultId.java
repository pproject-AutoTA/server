package com.autota.server.submission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SubmissionCaseResultId implements Serializable {

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "testcase_id")
    private Long testcaseId;
}
