# CLAUDE.md

- 체크리스트 기반 작업 현황(./reference/progress.md)을 참고하고, 작업이 끝나면 문서를 업데이트하고 git 커밋을 한다.
- 코드 작성 시 [아키텍처](./reference/architecture.md)를 준수

## References

- [기술 스택](./README.md#기술-스택) - 프로젝트에서 사용하는 기술 스택
- [아키텍처](./reference/architecture.md) - 클린 아키텍처 및 DDD 패턴, 패키지 구조, 네이밍 컨벤션
- [도메인 모델](./reference/domain-model.md) - MemberPoint 애그리거트 구조, LedgerEntry 기반 설계
- [요구사항](./reference/requirements.md) - 기능 요구사항, 예시 시나리오
- [DB 설계](./reference/database-design.md) - 스키마 (3개 테이블), 주요 쿼리, 연산별 처리 흐름
- [동시성 제어](./reference/concurrency-control.md) - Redis 분산락, 재시도 정책
- [시퀀스 다이어그램](./reference/sequence-diagrams.md) - 기능별 흐름 (Mermaid)
- [상태 다이어그램](./reference/state-diagrams.md) - 도메인 상태 변화 (Mermaid)
- [테스트 시나리오](./reference/test-scenarios.md) - 테스트 케이스 정의
- [구현 진행 상황](./reference/progress.md) - 체크리스트 기반 작업 현황
