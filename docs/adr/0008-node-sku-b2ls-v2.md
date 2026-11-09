# 0008. AKS 노드는 Standard_B2ls_v2 2대

상태: 채택 (2026-11-09, 생성 테스트로 확인)

## 맥락

기획서는 Standard_B2s(2 vCPU, 4 GiB) 2대였다. 서비스 노드 1대, 채점 노드 1대. 학생 구독의 vCPU 쿼터가 작아 그 이상은 어렵다고 봤다.

2026-11-09 koreacentral에서 확인한 것:

- `Standard_B2s`(v1)는 이 구독에 제공되지 않는다. `Standard_B2ls_v2`(2 vCPU, 4 GiB)와 `Standard_B2s_v2`(2 vCPU, 8 GiB)는 제공된다. 가격은 요금 계산기로 확인하기 전이다.
- 쿼터는 리전 전체 6 vCPU, Bsv2 패밀리 10 vCPU다. 기획서 가정(4)보다 넉넉하다.
- AKS 문서는 시스템 노드 풀에 4 vCPU 이상을 요구하고 B시리즈를 지원하지 않는다고 적고 있다. 그러나 B2ls_v2 시스템 풀 1대 + B2ls_v2 사용자 풀 1대(taint `autota/judge=true:NoSchedule`)를 실제로 만들어 보니 둘 다 생성됐다(AKS 1.36.5, Free tier). 테스트 후 삭제했다.

## 결정

노드는 `Standard_B2ls_v2` 2대다. 시스템 풀 1대에 서비스(core-api, judge-worker, lint-worker, web, RabbitMQ, Ingress)를 올리고, 사용자 풀 1대를 taint로 채점 Job 전용으로 쓴다. 리전은 koreacentral, OS 디스크는 30 GB.

## 결과

- 기획서와 같은 스펙이라 비용 계획의 틀을 유지한다. 채점 노드 분리도 유지한다.
- 감수할 것: 문서상 지원하지 않는 구성이라 Azure 정책이 바뀌면 생성이 막힐 수 있다. Terraform에 SKU를 고정하고, 막히면 4 vCPU 비B 시스템 노드 + B2ls_v2 채점 노드(6 vCPU, 비용 증가)로 바꾼다.
- 감수할 것: 쿼터 여유가 2 vCPU라 노드 풀 업그레이드 때 생기는 surge 노드 1대분밖에 없다. 두 풀을 동시에 업그레이드하면 쿼터를 넘는다. 업그레이드는 한 풀씩 한다.
- 감수할 것: `az aks create` 기본값은 노드 OS 자동 업그레이드 채널이 `NodeImage`라 노드가 자동으로 재이미징·재부팅될 수 있다. 시연 중 재부팅을 막기 위해 Terraform에서 채널을 `None`으로 두거나 유지 관리 시간대를 지정한다.
- 감수할 것: 4 GiB 노드에서 kubelet 예약이 25% 상한(약 1 GiB)에 걸린다. `maxPods`를 48 이하로 잡아 예약을 줄이고(생성 후 변경 불가), 앱마다 힙과 자원 요청량을 명시한다.
- 감수할 것: `az aks create` 기본값이 `networkPolicy: none`이라 Terraform에서 정책 엔진을 명시해야 NetworkPolicy가 동작한다. Standard Load Balancer와 공인 IP 1개가 자동 생성돼 과금된다.
