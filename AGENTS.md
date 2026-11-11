# AutoTA 서버

Python 과제 자동 채점 서비스의 서버 저장소. 레포 하나에 서비스 여러 개가 들어 있고, 서비스끼리는 RabbitMQ 큐 메시지로만 통신한다.

| 폴더 | 내용 | 담당 |
| --- | --- | --- |
| `core-api/` | Spring Boot API 서버. 도메인별 레이어드 구조 | A |
| `judge-worker/` | 채점 큐 소비, 채점 컨테이너 실행 | B |
| `judge-image/` | 학생 코드 실행용 이미지 | B |
| `lint-worker/` | Python 허용 요소 검사기 | A |
| `contracts/` | 큐 메시지 스키마. 서비스 간 유일한 접점 | A·B |
| `sql/` | 스키마, seed | A |
| `infra/` `deploy/` | Terraform, EC2 RabbitMQ compose(`infra/ec2/`), K8s 매니페스트 | B |

## 규칙 문서

- Git 컨벤션: `docs/conventions/git-convention.md`
- 아키텍처: `docs/architecture.md`
- ERD: `docs/erd.md`
- 결정 기록(ADR): `docs/adr/` (형식은 `docs/adr/README.md`)
- 큐 메시지 계약: `contracts/README.md`
- API 명세·기획서: Notion (저장소 밖)

커밋 메시지를 쓰거나 브랜치·PR을 만들기 전에 반드시 `docs/conventions/git-convention.md`를 읽는다. §5 체크리스트 순서대로 scope를 정하고 자가 검증까지 마친 뒤 작성한다.

## 작업할 때

- 다른 도메인의 데이터가 필요하면 그 도메인의 service를 호출한다. repository를 직접 쓰지 않는다.
- 큐 메시지(`contracts/`)는 필드 추가만 한다. 삭제·이름 변경은 A·B 합의 후 별도 PR.
- 큐·exchange는 서비스 코드에서 선언하지 않는다. RabbitMQ definitions(로컬 compose, `infra/ec2/`)로 만든다.
- 메시지 소비·재시도·멱등 규칙은 `contracts/README.md`를 따른다. 모르는 필드는 무시한다.
- 서비스마다 독립 프로젝트다(Gradle 또는 Python). 다른 서비스의 코드를 import하지 않는다.
- 커밋은 요청받았을 때만 한다. 커밋 메시지에 `Co-Authored-By`를 넣지 않는다.

## 절대 하지 않는 것

- `.env`, `*.tfstate`, 토큰·비밀번호를 커밋하지 않는다. 값은 `.env.example`에 키 이름만 남긴다.
- `sql/seed.sql`에 실제 학생·교수 정보를 넣지 않는다. 가짜 계정만.
- `CLAUDE.local.md`는 개인 설정 파일이다. 커밋하지 않는다.
