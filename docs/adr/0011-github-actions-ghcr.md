# 0011. CI/CD는 GitHub Actions, 이미지 저장소는 GHCR

상태: 채택 (2026-11-06, 기획서 v0.6)

## 맥락

트랙 요건 T3는 "푸시 → 이미지 빌드 → 저장소 → 배포"가 사람 손 없이 돌아가는 것이다. 코드가 GitHub 조직 `pproject-AutoTA`에 있다. 이미지 저장소는 Azure 안(ACR)에 둘 수도 있고 GitHub 안(GHCR)에 둘 수도 있다.

## 결정

CI/CD는 GitHub Actions, 컨테이너 이미지는 GHCR(`ghcr.io/pproject-autota/*`)에 둔다. 배포는 `main` 머지 시 워크플로가 이미지를 빌드·푸시한 뒤 AKS에 새 이미지를 적용한다. `develop`은 빌드·테스트까지만 한다.

| 대안 | 빠진 이유 |
| --- | --- |
| Azure Container Registry | 크레딧을 잠식하고, 구독이 비활성화되면 이미지도 같이 사라진다. 구독을 옮길 때 다시 만들어야 한다 |
| 다른 CI (Jenkins 등) | 호스팅할 곳과 운영 비용이 추가된다. 코드와 같은 곳에 있는 Actions가 가장 싸다 |

GHCR을 고른 핵심도 0005·0010과 같다. Azure 구독 상태와 무관한 곳에 두어야 구독을 옮겨도 이미지가 남는다. Actions에서는 `GITHUB_TOKEN`으로 GHCR에 바로 푸시할 수 있어 별도 자격 증명이 없다.

## 결과

- 저장소·CI·이미지가 모두 GitHub 안에 있어 설정이 한 곳에 모인다.
- 감수할 것: AKS가 GHCR에서 이미지를 받으려면 패키지를 공개로 두거나 `imagePullSecrets`(PAT)를 둬야 한다. 공개/비공개는 **(미정)**. 공개면 Secret이 필요 없지만 이미지 안에 설정값이 들어가지 않도록 해야 한다(설정은 환경 변수로, `docs/architecture.md` §5).
- 감수할 것: 배포 단계는 Actions가 AKS 자격 증명을 가져야 한다. 서비스 주체 또는 OIDC 연동 방식은 CI 이슈(T3)에서 정한다.
- 감수할 것: 서비스별 워크플로를 `paths:` 필터로 나누므로 필수 체크로 걸지 않는다(0009, git-convention §4).
