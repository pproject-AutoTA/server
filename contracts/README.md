# 큐 메시지 계약

core-api와 judge-worker·lint-worker가 만나는 유일한 접점이다. 여기 정의가 원본이고, 각 서비스는 이 정의를 보고 자기 폴더에 record(또는 dataclass)를 복사해 쓴다. 서비스끼리 코드를 import하지 않는다.

## 원칙

- 필드는 **추가만** 한다. 새 필드는 선택값으로 둔다. 삭제·이름 변경은 A·B 합의 후 별도 PR(git-convention §2).
- 소비자는 **모르는 필드를 무시**한다. 스키마의 `additionalProperties`는 열어 둔다.
- 이름은 camelCase(API 명세와 같음). 시각은 ISO-8601 UTC(`2026-11-20T06:08:04Z`). ID는 정수.
- 큐는 at-least-once다. 모든 메시지의 멱등 키는 `submissionId`이고, core-api는 메시지 종류별로 아래 규칙으로 중복을 거른다.
- 스키마는 JSON Schema 2020-12. 예시는 `examples/`에 있고 스키마를 통과해야 한다.

## 큐 구성

| 큐 | 타입 | 넣는 쪽 | 꺼내는 쪽 | 메시지 |
| --- | --- | --- | --- | --- |
| `judge.queue` | quorum, `delivery-limit` 3, DLX → `judge.dlq` | core-api | judge-worker | `JudgeRequest` |
| `lint.queue` | quorum, `delivery-limit` 3, DLX → `lint.dlq` | core-api | lint-worker (S1) | `LintRequest` |
| `result.queue` | quorum, `delivery-limit` 3, DLX → `result.dlq` | judge-worker, lint-worker | core-api | `JudgeStarted`, `JudgeResult`, `LintResult` |
| `judge.dlq` | quorum | 브로커(dead letter) | core-api | 한도 초과한 `JudgeRequest` |
| `lint.dlq`, `result.dlq` | quorum | 브로커 | 소비 안 함. 관리 UI에서 확인 | |

- 발행은 기본 exchange에 라우팅 키 = 큐 이름. dead letter는 `autota.dlx`(direct) → 각 `*.dlq`.
- quorum 큐의 dead letter 기본 전략 `at-most-once`는 전달 중 메시지를 잃을 수 있다. `judge.queue`·`lint.queue`는 `x-dead-letter-strategy=at-least-once`, `x-overflow=reject-publish`로 선언한다(둘 다 필요. `stream_queue` 피처 플래그가 켜져 있어야 한다).
- 메시지 속성: `content_type=application/json`, `delivery_mode=2`(persistent), `type`=메시지 이름(`JudgeRequest` 등), `message_id`=UUID.
- `result.queue`는 세 종류가 섞여 오므로 소비자는 `type` 속성으로 분기한다.
- RabbitMQ **4.3 이상**. 로컬은 평문 5672, 운영은 AMQPS 5671(ADR 0012).

## 메시지

| 이름 | 방향 | 언제 | 스키마 |
| --- | --- | --- | --- |
| `JudgeRequest` | core-api → judge-worker | 제출 저장 직후 | [judge-request.schema.json](judge-request.schema.json) |
| `JudgeStarted` | judge-worker → core-api | 채점 컨테이너를 띄우기 직전 | [judge-started.schema.json](judge-started.schema.json) |
| `JudgeResult` | judge-worker → core-api | 전 케이스 실행 완료(`DONE`) 또는 마지막 시도 실패(`FAILED`) | [judge-result.schema.json](judge-result.schema.json) |
| `LintRequest` | core-api → lint-worker | 제출 저장 직후 (S1) | [lint-request.schema.json](lint-request.schema.json) |
| `LintResult` | lint-worker → core-api | 검사 완료 (S1) | [lint-result.schema.json](lint-result.schema.json) |

### 판정 규칙 (JudgeResult)

- 케이스 verdict: `AC` `WA` `TLE` `MLE` `RE` `SKIPPED`. 정상이면 모든 케이스를 끝까지 실행한다(부분 점수 S2 때문). 출력 비교 규칙은 기획서 6절(줄 끝 공백·마지막 개행 무시).
- 컨테이너가 도중에 죽으면 실행 중이던 케이스는 종료 원인대로 판정하고(OOMKilled → `MLE`, 그 외 → `RE`), 뒤 케이스는 모두 `SKIPPED`다. `SKIPPED`인 케이스에는 `cpuTimeMs`·`memoryKb`·`actualOutput`을 넣지 않는다.
- 제출 verdict: 모두 `AC`면 `AC`, 아니면 `no` 순서로 처음 실패한 케이스의 verdict. `SKIPPED`가 처음 실패일 수는 없다(앞에서 죽었으므로). 혹시 그렇다면 `RE`로 둔다.
- `cpuTimeMs`는 케이스 실행 구간의 CPU 시간(user+sys, ms). ADR 0004대로 컨테이너 cgroup `cpu.stat`의 전후 차이다. `memoryKb`는 케이스 프로세스의 최대 RSS(KB, `wait4`의 `ru_maxrss`). 이 두 값의 측정 방식은 judge-image가 따르고, ADR 0004와 `docs/architecture.md` §4에도 같은 내용을 적는다.
- `actualOutput`은 1KB(1024바이트)에서 자른다. 출력이 1MB를 넘어 끊긴 케이스는 `RE`.
- FLAG는 verdict가 아니다. `LintResult.flagged`로 따로 온다.

### 메시지 크기

`JudgeRequest`는 테스트케이스 입력·기대 출력을 그대로 담는다(워커는 DB에 접근하지 않는다). RabbitMQ 4.x의 기본 `max_message_size`는 16 MiB다. core-api는 문제 등록(PROB-02) 때 테스트케이스 입력·기대 출력 합계를 **8 MB 이하**로 제한한다 **(제안. API 명세에 없는 검증이라 A가 확정)**.

### 실패 처리 (judge-worker)

학생 코드 때문에 생기는 문제(RE·TLE·MLE·출력 초과)는 실패가 아니라 정상 `DONE` 결과다. 아래는 **채점 자체가 안 된 경우**(컨테이너 생성 실패, 이미지 없음, Job 스케줄 실패, 런너 예외)다.

1. 시도 횟수는 메시지 헤더 `x-delivery-count`(실패한 전달 횟수. 재전달되는 메시지에 항상 붙는다)로 안다. `attempt = x-delivery-count + 1`이고, 헤더가 없으면 첫 시도(1)다. 헤더가 없는데 `redelivered` 플래그가 true면 횟수를 알 수 없으므로 재시도하지 않고 바로 3번으로 간다(무한 재시도보다 실패가 낫다).
2. 시도가 **4회 미만**이면 **`basic.reject(requeue=true)`**로 바로 돌려보낸다. `basic.nack`는 쓰지 않는다. quorum 큐 문서의 표에서 `basic.reject`는 `delivery-count`를 올리고 `basic.nack`는 올리지 않는다. nack로 돌려보내면 한도가 영영 걸리지 않는다. 대기는 넣지 않는다. prefetch 1인 워커가 자는 동안 다른 제출이 막히고, 인프라 실패는 몇 초 기다린다고 풀리지 않는다(Job 스케줄 대기는 이미 60초).
3. **4회째**(`x-delivery-count` = 3)면 `JudgeResult`를 `status=FAILED`, `attempt=4`, `error`를 담아 `result.queue`에 발행하고 ack한다. 실패 이력은 이 메시지로 남는다(ERD `judge_failures`).
4. 브로커의 `delivery-limit` 3은 2차 안전장치다. 재전달이 3회를 넘으면(즉 4회째 전달도 실패하면) 브로커가 `judge.dlq`로 보낸다. 워커가 3번을 하기 전에 죽은 경우(연결 끊김도 `delivery-count`에 집계된다)가 여기 걸린다. core-api가 `judge.dlq`를 소비해 `FAILED`로 바꾸고, `reason`은 `x-first-death-reason`(`delivery_limit`), `retry_count`는 `x-death`의 정보로 채운다.

워커의 4회 상한과 브로커 한도 3은 같은 카운터(`delivery-count`)를 보므로 서로 어긋나지 않는다. 4.3 이전 버전은 한도 기준이 달라 이 설계가 맞지 않는다. 4.3 이상을 전제로 한다.

### 멱등 규칙 (core-api)

| 메시지 | 처리 |
| --- | --- |
| `JudgeStarted` | `status = PENDING`일 때만 `RUNNING`으로 바꾼다. 이미 `RUNNING`·`DONE`·`FAILED`면 무시 |
| `JudgeResult` `DONE` | `status`가 `DONE`이 아닐 때만 반영한다(`PENDING`·`RUNNING`·`FAILED` → `DONE`). 케이스 결과는 `UNIQUE (submission_id, testcase_id)`로 중복 삽입을 막는다. `DONE`은 바뀌지 않는다 |
| `JudgeResult` `FAILED` | `status`가 `PENDING`·`RUNNING`일 때만 반영한다 |
| `judge.dlq` 메시지 | `status`가 `PENDING`·`RUNNING`일 때만 `FAILED`로 바꾼다 |
| `LintResult` | `lint_results.submission_id UNIQUE` |

`JudgeStarted`가 `JudgeResult`보다 늦게 와도 위 규칙으로 무시된다. `FAILED` 뒤에 `DONE`이 오는 경우(워커는 끝까지 채점했는데 ack가 유실돼 다른 경로로 먼저 `FAILED`가 된 경우)는 `DONE`이 덮어쓴다. 실제 결과가 있는데 실패로 남기는 것보다 낫다.

lint-worker의 채점 외 실패(큐 소비·파싱 외의 예외) 처리는 S1 착수 때 정한다. Must에서는 lint 메시지가 오가지 않는다.

### 소비자 공통

- 수동 ack. 결과를 DB에 반영(또는 큐에 발행)한 뒤 ack한다.
- judge-worker는 prefetch 1(채점은 한 번에 하나). 처리량은 워커 수로 늘린다.
- 파싱 실패(스키마 위반)는 재시도해도 소용없으므로 `basic.reject(requeue=false)`로 바로 dead letter 보낸다.

## 구현 메모

- **큐·exchange 선언은 서비스가 하지 않는다.** 로컬 compose와 `infra/ec2`의 RabbitMQ에 definitions 파일로 미리 만든다. 서비스마다 선언하면 인자(`x-queue-type`, `x-delivery-limit` 등)가 하나라도 다를 때 `PRECONDITION_FAILED`로 채널이 닫힌다.
- **Spring AMQP(core-api, judge-worker):** 기본 `Jackson2JsonMessageConverter`는 `__TypeId__` 헤더로 타입을 고르는데 이 계약은 AMQP `type` 속성을 쓴다. 발행 시 `MessageProperties.setType`을 넣고, `result.queue` 소비자는 `Message`로 받아 `type`으로 분기한 뒤 직접 역직렬화한다. Jackson은 `FAIL_ON_UNKNOWN_PROPERTIES=false`(모르는 필드 무시), 시각은 ISO-8601 문자열(`WRITE_DATES_AS_TIMESTAMPS=false`)로 둔다.
- **pika(lint-worker):** `properties.type`, `properties.content_type`, `delivery_mode=2`를 넣는다.
- **길이 단위:** 스키마의 `maxLength`는 문자 수다. 64KB(코드)·1KB(출력) 같은 바이트 상한은 보내는 쪽이 바이트로 자르거나 검사한다. 바이트 수는 문자 수 이상이므로 스키마 검증은 보조다.
- 스키마 검증은 테스트에서 쓴다(Java `networknt/json-schema-validator`, Python `jsonschema`, 둘 다 2020-12 지원). 런타임 검증은 선택.

## 예시 검증

```bash
pip install jsonschema
python -c "import json,jsonschema,sys; s=json.load(open('contracts/judge-result.schema.json')); jsonschema.validate(json.load(open('contracts/examples/judge-result-done.json')), s); print('ok')"
```

## 변경 절차

1. 이 폴더의 스키마·예시를 고치는 `docs(contract)` 커밋을 먼저 만든다.
2. 같은 PR에서 보내는 쪽과 받는 쪽 코드를 둘 다 고친다.
3. 상대 담당이 리뷰한다.
