# Git 컨벤션

커밋(Angular 기반) · 브랜치(main/develop) · 이슈/PR 규칙. 커밋·브랜치 생성·PR 작성 시 참조.

서버는 레포 하나(`server/`)에 서비스 여러 개가 들어 있다. 그래서 scope는 **서비스 또는 core-api 도메인** 단위로 잡는다.

---

## 1. 커밋 메시지

### 기본 구조 (싱글 라인)

```
git commit -m "<type>(<scope>): <subject> (#issue)"
```

- `type`: 커밋 종류 (필수)
- `scope`: 변경 범위 (필수, §2 표에서 고른다. 루트 잡무만 생략 가능)
- `subject`: 간결한 설명 (필수)
- body / footer: 상세·이슈 참조 (선택)

### Type 종류

| Type | 설명 | 예시 |
| --- | --- | --- |
| feat | 새 기능 | `feat(submission): 코드 제출 API 추가` |
| fix | 버그 수정 | `fix(judge): 출력 1MB 초과 시 RE 판정 누락 수정` |
| docs | 문서 | `docs(contract): JudgeResult 예시 갱신` |
| refactor | 리팩토링 | `refactor(course): 수업 참여 검증 로직 분리` |
| perf | 성능 | `perf(lint): ast 순회 1회로 축소` |
| test | 테스트 | `test(judge): TLE 판정 단위 테스트 추가` |
| build | 빌드·의존성·Dockerfile | `build(judge-image): python 3.12로 변경` |
| ci | GitHub Actions | `ci(github): core-api 워크플로에 paths 필터 추가` |
| chore | 기타(소스 변경 X) | `chore: .gitignore에 tfstate 추가` |
| revert | 되돌리기 | `revert: feat(problem) 되돌림` |

### 작성 규칙

- **Subject**: 50자 이내 · 마침표 금지 · 명령문("추가함" → "추가") · 한국어로 통일
- **Body**: 무엇을·왜 (어떻게는 코드가 말한다)
- **Footer**: 이슈 참조 `Closes #123` / `Fixes #456`
- **MUST NOT**: 커밋 메시지에 `Co-Authored-By` 트레일러를 넣지 않는다.

### 좋은 예 / 나쁜 예

```
✅ feat(submission): 채점 요청 큐 발행 (#21)
✅ fix(judge): 메모리 초과를 RE로 판정하던 문제 수정 (#34)
✅ refactor(problem): 테스트케이스 조회를 ProblemService로 이동

❌ 수정함   ❌ Update code   ❌ feat: 작업
❌ 버그수정.   ❌ FIX: BUG FIX!!!
```

---

## 2. Scope

### core-api (도메인 = scope)

| scope | 범위 |
| --- | --- |
| auth | 로그인 3종(학생·교수·관리자), JWT 발급 |
| user | 사용자·역할 |
| course | 수업, 수업 참여 코드 |
| assignment | 과제, 마감 |
| problem | 문제, 테스트케이스, 허용 요소 정책 |
| submission | 제출, 채점 결과 저장, 큐 발행·결과 리스너 |
| admin | 시스템 현황, 채점 실패 이력 |
| global | 공통 응답, 예외·에러코드, 보안 필터, 설정 |

### 그 외 폴더 (서비스 = scope)

| scope | 폴더 | 범위 |
| --- | --- | --- |
| judge | `judge-worker/` | 큐 소비, Runner, 출력 비교·판정 |
| judge-image | `judge-image/` | 학생 코드 실행 이미지, run.py |
| lint | `lint-worker/` | 허용 요소 검사기 |
| contract | `contracts/` | 큐 메시지 스키마·예시 |
| db | `sql/` | schema.sql, seed.sql |
| infra | `infra/` | Terraform (AKS, 네트워크), 학교 EC2 RabbitMQ compose |
| k8s | `deploy/` | K8s 매니페스트, NetworkPolicy, RBAC |
| compose | `docker-compose.yml` | 로컬 실행 환경 |
| github | `.github/` | 워크플로, CODEOWNERS, 템플릿 |
| docs | `docs/`, `AGENTS.md`, `CLAUDE.md`, `README.md` | 컨벤션 문서, 에이전트 지침, README |

### 커밋 단위 규칙

- **scope 하나 = 커밋 하나** — 한 도메인(또는 서비스) 안에서 끝낸다
- **type 하나로 설명되면 적정** — 두 type이 필요하면 쪼갠다
- **각 커밋은 해당 서비스 빌드 + 테스트 통과** — 컴파일 안 되는 중간 상태 금지
- **도메인에 걸치면 피호출 측 먼저** — 호출당하는 도메인의 service 메서드를 먼저, 호출하는 쪽을 나중에
- **global 변경은 독립 커밋** — 모든 도메인에 영향이 가니 기능 커밋에 섞지 않는다
- **DB 변경은 db 먼저** — `sql/` 커밋 다음에 엔티티·repository 커밋

```
feat(problem): 공개 테스트케이스 조회 메서드 추가
feat(submission): 제출 시 문제 공개 케이스 검증 (#21)
feat(global): SUBMISSION 에러코드 추가
```

### 큐 메시지(contracts) 변경 규칙

core-api와 judge-worker·lint-worker가 만나는 유일한 지점이라 별도로 정한다.

1. `contract` 커밋으로 스키마·예시·`contracts/README.md`를 먼저 바꾼다. `examples/`가 스키마를 통과해야 한다
2. 같은 PR 안에서 보내는 쪽과 받는 쪽을 둘 다 고친다 (`submission` + `judge`, 또는 `submission` + `lint`)
3. 필드는 **추가만** 하고, 새 필드는 선택값으로 둔다. 이름 변경·삭제는 A·B 합의 후 별도 PR로
4. 이 PR은 상대 담당의 Approve가 필수다. 작성자는 자기 PR을 승인할 수 없으므로, 보내는 쪽·받는 쪽 수정 내용과 예시 검증 결과를 PR 본문에 적는 것으로 작성자 몫을 대신한다

```
docs(contract): JudgeResult에 memoryKb 필드 추가
feat(judge): 케이스별 메모리 사용량 측정
feat(submission): 케이스별 메모리 사용량 저장 (#40)
```

---

## 3. 브랜치 전략

배포는 시연 1회가 목표라 Git Flow에서 release를 뺀 형태로 간다.

| 브랜치 | 역할 | 분기 | 머지 |
| --- | --- | --- | --- |
| main | 배포 (push 시 AKS 배포) | - | - |
| develop | 개발 통합 (push 시 빌드·테스트만) | main | main |
| feat/* | 기능 개발 | develop | develop |
| fix/* | 버그 수정 | develop | develop |
| refactor/* | 리팩토링 | develop | develop |
| hotfix/* | 배포본 긴급 수정 | main | main, develop |
| docs/* | 문서·컨벤션 | develop | develop |
| init/* | 초기 세팅 (구조·빌드) | develop | develop |
| chore/* | 기타 설정·잡무 (소스 변경 X) | develop | develop |

배포가 main에서만 나가는 이유: 배포 한 번마다 이미지 푸시·롤아웃이 일어나고, 클러스터 크레딧이 한정돼 있다. develop에 쌓았다가 묶어서 main으로 올린다.

### 네이밍

```
feat/#{이슈번호}/{기능명}       예: feat/#12/submission-api
fix/#{이슈번호}/{버그명}        예: fix/#34/memory-verdict
refactor/#{이슈번호}/{주제}     예: refactor/#30/problem-service
hotfix/#{이슈번호}/{버그명}     예: hotfix/#45/judge-timeout
docs/#{이슈번호}/{주제}         예: docs/#2/git-convention
init/#{이슈번호}/{주제}         예: init/#1/server-structure
chore/#{이슈번호}/{주제}        예: chore/#7/gitignore-update
```

```
✅ feat/#12/submission-api   ✅ hotfix/#45/judge-timeout   ✅ init/#1/server-structure
❌ feat/submission(이슈 번호 없음)   ❌ Feat/Submission(대문자)
```

### 규칙

- 작업 브랜치는 main 직접 머지 금지 (→ develop). hotfix만 main에 머지하고 develop에 역머지
- 작업 전 항상 `git pull`
- 한 브랜치 = 하나의 기능. 서비스 두 개를 건드려도 기능이 하나면 한 브랜치 (예: contracts 변경)
- 머지된 브랜치는 즉시 삭제
- 브랜치 이동 시 `git stash` 활용

---

## 4. 이슈 / PR

### 이슈 제목

```
[TYPE] 작업 내용   (예: [Feat] 코드 제출 API)
```

### PR 규칙

- PR 제목은 이슈 제목과 같은 `[Type] 작업 내용 (#이슈번호)` 형식 (커밋의 `type(scope): subject`와 다름)
- PR 템플릿 모든 항목 작성
- 리뷰어는 CODEOWNERS로 자동 지정. **상대 서버 담당 1명 Approve** 후 머지
  - `contracts/` 변경 PR은 상대 담당 Approve 필수. 작성자 쪽 확인은 PR 본문에 적는다(§2)
- 변경된 서비스의 CI(빌드·테스트)가 통과한 뒤 머지
- 머지 후 브랜치 삭제

> 서비스별 워크플로는 `paths:` 필터로 돌아서, 건드리지 않은 서비스의 체크는 아예 실행되지 않는다. GitHub는 path 필터로 건너뛴 워크플로의 필수 체크를 Pending으로 남겨 머지를 막으므로, 서비스별 체크를 브랜치 보호의 필수 체크로 걸지 않는다.

### PR 본문 항목

작업 내용(What) · 변경 사항(Details) · 영향 받는 서비스 · 큐 메시지 변경 여부 · 주의 사항 · 관련 이슈(`Closes #N`) · 체크리스트(빌드/테스트 완료, 리뷰 반영).

GitHub 이슈/PR 템플릿 파일은 `.github/`에 둔다 (`ISSUE_TEMPLATE/`, `pull_request_template.md`).

---

## 5. 커밋 전 체크리스트

AI 에이전트에게 커밋·브랜치·PR을 맡길 때도 이 순서대로 한다.

### scope 정하기

`git status`와 `git diff --stat`으로 바뀐 경로를 보고 §2 표에서 scope를 고른다. 여러 scope에 걸치면 scope별로 커밋을 나눈다.

| 바뀐 경로 | scope |
| --- | --- |
| `core-api/.../domain/{도메인}/` | 도메인명 (auth, user, course, assignment, problem, submission, admin) |
| `core-api/.../global/` | global |
| `judge-worker/` | judge |
| `judge-image/` | judge-image |
| `lint-worker/` | lint |
| `contracts/` | contract |
| `sql/` | db |
| `infra/` | infra |
| `deploy/` | k8s |
| `docker-compose.yml` | compose |
| `.github/` | github |
| `docs/`, `AGENTS.md`, `CLAUDE.md`, `README.md` | docs |

### 자가 검증

- 커밋 type이 §1 목록에 있는가
- scope가 §2 표에 있는 값 1개이고, 한 커밋이 그 범위 안에서 끝나는가
- subject가 50자 이내·마침표 없음·명령문·한국어인가
- 커밋 메시지에 `Co-Authored-By` 트레일러가 없는가
- 각 커밋에서 해당 서비스가 빌드되고 테스트가 통과하는가
- 도메인에 걸치면 피호출 측 먼저, 호출 측 나중으로 나눴는가
- global 변경, sql 변경을 독립 커밋으로 분리했는가
- contracts를 바꿨다면 보내는 쪽과 받는 쪽을 같은 PR에서 고쳤고, 기존 필드를 지우거나 이름을 바꾸지 않았는가. `contracts/examples/`가 스키마를 통과하는가
- 브랜치명이 §3 패턴이고 소문자인가 (`{type}/#{이슈}/{주제}`)
- 머지 대상이 맞는가 — 작업 브랜치 → develop, hotfix → main·develop (작업 브랜치 → main 직접 금지)
