# 0002. 메시지 브로커는 RabbitMQ

상태: 채택 (2026-11-06, 기획서 v0.6)

## 맥락

0001에서 큐 통신을 정했으니 브로커가 필요하다. 필요한 것은 네 가지다. 실패한 채점의 재시도, 반복 실패하는 메시지의 격리, 큐 적재량에 따른 동시 실행 조절, 관리자 화면(M7)에 보여 줄 큐 적재량과 실패 이력. 메시지 순서 보장은 필요 없다.

## 결정

RabbitMQ 4.3 이상을 쓴다. 큐는 `judge.queue`, `lint.queue`, `result.queue` 세 개이고 **quorum 타입**으로 만든다. 반복 실패 격리는 quorum 큐의 `delivery-limit`(4.0부터 기본 20)으로 하고, 기능 명세의 "3회 재시도"에 맞춰 값을 정한다. 한도를 넘긴 메시지는 dead letter 큐로 보낸다. 재전달 횟수는 `x-delivery-count` 헤더로 확인한다. `x-death`의 `count`는 dead-letter된 횟수라 재시도 횟수가 아니다.

4.3부터 한도는 `delivery-count` 기준이다. 문서의 표대로 `basic.reject`와 연결 끊김은 집계되고 `basic.nack`는 집계되지 않는다. 그래서 소비자는 실패 시 **`basic.reject(requeue=true)`만** 쓰고 nack는 쓰지 않는다. dead letter는 기본 `at-most-once`가 유실될 수 있어 `dead-letter-strategy=at-least-once`(`overflow=reject-publish` 필요)로 선언한다. 세부 규칙은 `contracts/README.md`.

quorum 큐의 실제 이점은 poison message 격리(`delivery-limit`)다. 지연 재시도(back-off)는 4.3부터 있고 기본 비활성(`x-delayed-retry-type: disabled`)이라 필요하면 따로 켠다. 클래식 큐는 DLX + TTL 사이클을 직접 구성해야 한다.

검토한 대안:

| 대안 | 빠진 이유 |
| --- | --- |
| Kafka | 처리량 규모가 과하다. 파티션 수가 소비자 병렬성의 상한이 되고, 재시도와 실패 격리를 직접 만들어야 한다 |
| Redis (리스트·스트림) | 재시도와 실패 격리를 직접 만들어야 하고, 큐 적재량·실패 메시지를 큐 개념으로 보여 주는 관리 화면이 없다 |

## 결과

- 반복 실패 격리와 재시도 횟수 추적을 브로커 설정으로 해결한다.
- 관리 API로 큐 적재량을 바로 읽는다. KEDA의 RabbitMQ 스케일러가 `QueueLength`·`MessageRate` 모드를 제공해 동시 실행 조절(C2)로 이어진다. rate 모드는 관리 API(HTTP) 접근이 필요하다.
- 감수할 것: dead letter 큐는 격리 수단이지 이력 저장소가 아니다. 소비하거나 비우면 사라진다. M7의 채점 실패 이력은 DB에 남긴다. judge-worker가 마지막(4회째) 시도에서 사유를 담은 `JudgeResult FAILED`를 발행하고, 워커가 죽어 브로커 한도에 걸린 경우만 core-api의 dead letter 소비자가 적는다(`contracts/README.md`).
- 감수할 것: 브로커를 직접 운영한다. 위치는 처음에 서비스 노드였으나 0012에서 클러스터 밖(학교 EC2)으로 옮겼다. quorum 큐는 클래식보다 메모리를 더 쓰므로 EC2 사양을 정할 때 감안한다.
- 감수할 것: 브로커 하나가 단일 장애점이다. 시연 규모에서는 받아들인다.
