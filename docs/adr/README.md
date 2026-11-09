# ADR

아키텍처 결정 기록. 왜 이렇게 정했는지를 남긴다. 무엇이 어디서 도는지는 `docs/architecture.md`에 있다.

## 규칙

- 결정 하나 = 파일 하나. 파일명은 `NNNN-주제.md`(번호 4자리, 영문 소문자와 하이픈).
- 항목은 **상태 · 맥락 · 결정 · 결과** 네 개. 한 페이지를 넘기지 않는다. 근거 자료는 링크로 건다.
- 상태는 `제안` → `채택`, 또는 `대체됨 → NNNN`. 결정을 바꿀 때는 옛 파일을 고치지 않고 새 번호로 쓴 뒤 옛 파일의 상태만 바꾼다.
- 새 결정은 번호를 이어서 추가하고 아래 목록에 넣는다.

## 목록

| 번호 | 결정 | 상태 |
| --- | --- | --- |
| [0001](0001-queue-only-communication.md) | 서비스 간 통신은 큐 메시지로만 한다 | 채택 |
| [0002](0002-rabbitmq.md) | 메시지 브로커는 RabbitMQ | 채택 |
| [0003](0003-no-judge0.md) | Judge0을 쓰지 않고 컨테이너 격리를 직접 구성한다 | 채택 |
| [0004](0004-cpu-time-limit.md) | 시간 제한은 CPU 시간으로 판정한다 | 채택 |
| [0005](0005-database-outside-cluster.md) | DB는 클러스터 밖(Supabase PostgreSQL)에 둔다 | 채택 |
| [0006](0006-judge-runner-abstraction.md) | 채점 실행부를 인터페이스로 추상화한다 | 채택 |
| [0007](0007-self-issued-jwt.md) | 인증은 core-api가 JWT를 직접 발급한다 | 제안 |
| [0008](0008-node-sku-b2ls-v2.md) | AKS 노드는 Standard_B2ls_v2 2대 | 채택 |
| [0009](0009-monorepo.md) | 서버는 레포 하나에 서비스별 폴더, 클라이언트는 별도 레포 | 채택 |
| [0010](0010-azure-aks.md) | 클라우드는 Azure, 오케스트레이션은 AKS | 채택 |
| [0011](0011-github-actions-ghcr.md) | CI/CD는 GitHub Actions, 이미지 저장소는 GHCR | 채택 |
